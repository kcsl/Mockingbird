package com.kcsl.fuzzing.mockingbird.ui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.kcsl.fuzzing.mockingbird.analysis.FuzzerConfiguration;

public class Mockingbird {

	public static File generateConfig(Q methods) throws IOException {
		AtlasSet<Node> nodes = new AtlasHashSet<Node>(methods.nodes(XCSG.Method).eval().nodes());
		if(nodes.isEmpty()) {
			throw new IllegalArgumentException("Please select a method!");
		}
		if(nodes.size() > 1) {
			throw new IllegalArgumentException("Multiple methods not supported yet!");
		}
		return generateConfig(nodes.one());
	}
	
	public static File generateConfig(Node method) throws IOException {
		FuzzerConfiguration config = new FuzzerConfiguration(1000, "CONFIG");
		config.setMethod(method);
		File configFile = File.createTempFile("config", ".json");
		FileWriter configFileWriter = new FileWriter(configFile);
		configFileWriter.write(config.toString(true));
		configFileWriter.close();
//		WorkspaceUtils.openFileInEclipseEditor(configFile);
		return configFile;
	}
	
}
