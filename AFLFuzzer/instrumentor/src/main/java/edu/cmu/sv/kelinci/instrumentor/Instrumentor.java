package edu.cmu.sv.kelinci.instrumentor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

/**
 * @author rodykers
 *
 * Instrumentation component of Kelinci. Adds AFL style instrumentation to a JAVA
 * program, plus the Kelinci 'javaside' component.
 */
public class Instrumentor {
	
	public static ClassLoader classloader;
	
	public static void main(String[] args) {
	
		// get class loader
		classloader = Thread.currentThread().getContextClassLoader();
		
		// parse command line arguments
		Options options = Options.v();
		CmdLineParser parser = new CmdLineParser(options);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			parser.printUsage(System.err);
			return;
		}

		// load all classes to instrument (instrument library classes?)
		Set<String> inputClasses = Options.v().getInput();
		
		Set<String> skipped = new HashSet<>();
		
		for (String cls : inputClasses) {
			System.out.println("Instrumenting class: " + cls);
			InputStream bytecode = classloader.getResourceAsStream(cls);
			
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			ClassTransformer ct = new ClassTransformer(cw);
			ClassReader cr;
			try {
				cr = new ClassReader(bytecode);
			} catch (IOException | NullPointerException e) {
				System.err.println("Error loading class: " + cls);
				e.printStackTrace();
				return;
			}
			
			try {
				cr.accept(ct, 0);
				byte[] bytes = cw.toByteArray();
				writeClass(cls, bytes);
			} catch (RuntimeException rte) {
				if (rte.getMessage().contains("JSR/RET")) {
					/**
					 * This is an exception related to a particular construct in the bytecode that
					 * is not supported by ASM. It is deprecated and should not be present in bytecode
					 * generated by a recent compiler. However, the JDK contains it and it may occur elsewhere.
					 * This catch simply skips the class and warns the user.
					 */
					System.out.println("\n[WARNING] RuntimeException thrown during instrumentation: " + rte.getMessage());
					System.out.println("Skipping instrumentation of class " + cls + "\n");
					// include original, uninstrumented class in output
					loadAndWriteResource(cls);
					skipped.add(cls);
				} else {
					throw rte;
				}
			}
			
		}

		// add Kelinci classes
		String[] resources = {
				"edu/cmu/sv/kelinci/Kelinci.class",
				"edu/cmu/sv/kelinci/Kelinci$1.class",
				"edu/cmu/sv/kelinci/Kelinci$2.class",
				"edu/cmu/sv/kelinci/Kelinci$ApplicationCall.class",
				"edu/cmu/sv/kelinci/Kelinci$FuzzRequest.class",
				"edu/cmu/sv/kelinci/Kelinci$NullOutputStream.class",
				"edu/cmu/sv/kelinci/Mem.class"
				};
		for (String resource : resources) {
			loadAndWriteResource(resource);
		}
		
		if (skipped.size() > 0) {
			System.out.println("\nWARNING!!! Instrumentation of some classes has been skipped.");
			System.out.println("This is due to the JSR/RET bytecode construct that is not supported by ASM.");
			System.out.println("It is deprecated and should not be present in bytecode generated by a recent compiler.");
			System.out.println("If this is your code, try using a different compiler.");
			System.out.println("If this is a library, there might be not too much harm in skipping instrumentation of these classes.");
			System.out.println("Classes that were skipped:");
			for (String cls : skipped)
				System.out.println(cls);
		}
	}

	private static void writeClass(String cls, byte[] bytes) {
		if (Options.v().outputJar()) {
			JarFileIO.v().addFileToJar(cls, bytes);
		} else {
			String path = Options.v().getOutput().endsWith("/") ? 
					Options.v().getOutput() + cls : 
						Options.v().getOutput() + "/" + cls;
			Path out = Paths.get(path);
			try {
				Files.createDirectories(out.getParent());
				Files.write(out, bytes);
				System.out.println("File written: " + path);
			} catch (IOException e) {
				System.err.println("Error writing to file: " + path);
				e.printStackTrace();
			}
		}
	}

	private static void loadAndWriteResource(String resource) {
		InputStream is = classloader.getResourceAsStream(resource);
		if (is == null) {
			System.err.println("Error loading Kelinci classes for addition to output");
			return;
		}
		byte[] rbytes;
		try {
			rbytes = IOUtils.toByteArray(is);
		} catch (IOException e) {
			System.err.println("Error loading Kelinci classes for addition to output");
			e.printStackTrace();
			return;
		}
		writeClass(resource, rbytes);
	}
}