package com.kcsl.fuzzing.mockingbird.analysis;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.java.commons.analysis.CommonQueries;
import com.kcsl.fuzzing.mockingbird.analysis.MethodAnalysis.Parameter;

public class FuzzerConfiguration {

	private static final String CONSOLE_LOG_LEVEL = "console_log_level";
	private static final String TIMEOUT = "timeout";

	public static void main(String[] args) throws Exception {
		System.out.println(new FuzzerConfiguration(1000, "CONFIG").toString());
	}
	
	private JSONObject fuzzerConfiguration;
	
	@SuppressWarnings("unchecked")
	public FuzzerConfiguration(long timeout, String logLevel) {
		this.fuzzerConfiguration = new JSONObject();
		JSONObject configuration = new JSONObject();
		configuration.put(TIMEOUT, timeout);
		configuration.put(CONSOLE_LOG_LEVEL, logLevel);
		fuzzerConfiguration.put("config", configuration);
	}
	
	@SuppressWarnings("unchecked")
	public void setTimeout(long timeout) {
		JSONObject configuration = (JSONObject) fuzzerConfiguration.get("config");
		configuration.put(TIMEOUT, timeout);
	}
	
	@SuppressWarnings("unchecked")
	public void setLogLevel(String logLevel) {
		JSONObject configuration = (JSONObject) fuzzerConfiguration.get(CONSOLE_LOG_LEVEL);
		configuration.put(CONSOLE_LOG_LEVEL, logLevel);
	}
	
	@SuppressWarnings("unchecked")
	public void setMethod(Node method) {
		if(!method.taggedWith(XCSG.Method)) {
			throw new IllegalArgumentException("Expected a method!");
		}
		JSONObject definition = new JSONObject();
		Node ownerClass = MethodAnalysis.getOwnerClass(method);
		String pkg = ClassAnalysis.getPackage(ownerClass);
		definition.put("class", pkg + (pkg.isEmpty() ? "" : ".") + ClassAnalysis.getName(ownerClass));
		definition.put("method", MethodAnalysis.getName(method));
		JSONArray parameters = new JSONArray();
		for(Parameter parameter : MethodAnalysis.getParameters(method)) {
			JSONObject configurationParameter = new JSONObject();
			configurationParameter.put("class", parameter.getType());
			configurationParameter.put("name", parameter.getName());
			
			if(parameter.isPrimitive()) {
				JSONObject noConstraint = new JSONObject();
				noConstraint.put("type", "default");
				configurationParameter.put("constraint", noConstraint);
			} else {
				JSONArray parameterUsedMethods = new JSONArray();
				
				// TODO: add methods called on the parameter
				
				configurationParameter.put("methods", parameterUsedMethods);
				
				
				JSONArray parameterInstanceVariables = new JSONArray();
				
				// TODO: add instance variable accesses
				
				configurationParameter.put("instance_variables", parameterInstanceVariables);
			}
			
			parameters.add(configurationParameter);
		}
		definition.put("parameters", parameters);
		
		Q interproceduralDataFlowEdges = Common.universe().edges(XCSG.InterproceduralDataFlow);
		Q instanceVariables = interproceduralDataFlowEdges.predecessors(CommonQueries.localDeclarations(Common.toQ(method))).nodes(XCSG.InstanceVariable);
		for(Node instanceVariable : instanceVariables.eval().nodes()) {
			
		}
		
		fuzzerConfiguration.put("definition", definition);
	}
	
	public String toString() {
		return fuzzerConfiguration.toJSONString();
	}
	
}
