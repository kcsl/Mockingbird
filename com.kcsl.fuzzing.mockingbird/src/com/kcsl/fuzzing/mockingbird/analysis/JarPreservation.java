package com.kcsl.fuzzing.mockingbird.analysis;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarException;

public class JarPreservation {

	public static void copyJarResources(File originalJar, File generatedJar, File outputJar) throws JarException, IOException {
		JarInspector inspector = new JarInspector(originalJar);
		JarModifier modifier = new JarModifier(generatedJar);
		// copy over the original jar resources
		for(String entry : inspector.getJarEntrySet()){
			if(!entry.endsWith(".class")){
				byte[] bytes = inspector.extractEntry(entry);
				modifier.add(entry, bytes, true);
			}
		}
		modifier.save(outputJar);
	}
	
}
