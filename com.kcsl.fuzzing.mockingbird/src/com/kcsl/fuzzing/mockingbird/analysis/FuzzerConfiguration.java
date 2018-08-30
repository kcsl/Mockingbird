package com.kcsl.fuzzing.mockingbird.analysis;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.java.commons.analysis.CommonQueries;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.kcsl.fuzzing.mockingbird.analysis.MethodAnalysis.Parameter;

public class FuzzerConfiguration {

	private static final String CONSOLE_LOG_LEVEL = "console_log_level";
	private static final String TIMEOUT = "timeout";

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
		Q dataFlowEdges = Common.universe().edges(XCSG.DataFlow_Edge);
		Q localDataFlowEdges = Common.universe().edges(XCSG.LocalDataFlow);
		Q interproceduralDataFlowEdges = Common.universe().edges(XCSG.InterproceduralDataFlow);
		
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
				
				// add methods called directly on the parameter
				Q identityPass = localDataFlowEdges.successors(Common.toQ(parameter.getParameterNode())).nodes(XCSG.IdentityPass);
				Q identities = interproceduralDataFlowEdges.successors(identityPass).nodes(XCSG.Identity);
				Q invokedIdentityMethods = CommonQueries.getContainingMethods(identities);
				for(Node invokedIdentityMethod : invokedIdentityMethods.eval().nodes()) {
					JSONObject invokedMethod = new JSONObject();
					invokedMethod.put("name", invokedIdentityMethod.getAttr(XCSG.name).toString());
					JSONObject noConstraint = new JSONObject();
					noConstraint.put("type", "default");
					invokedMethod.put("constraint", noConstraint);
					parameterUsedMethods.add(invokedMethod);
				}
				
				configurationParameter.put("methods", parameterUsedMethods);
				
				
				JSONArray parameterInstanceVariables = new JSONArray();
				
				// TODO: add instance variable accesses directly
				
				configurationParameter.put("instance_variables", parameterInstanceVariables);
			}
			
			parameters.add(configurationParameter);
		}
		definition.put("parameters", parameters);
		
		Q instanceVariables = interproceduralDataFlowEdges.predecessors(CommonQueries.localDeclarations(Common.toQ(method))).nodes(XCSG.InstanceVariable);
		for(Node instanceVariable : instanceVariables.eval().nodes()) {
			
		}
		
		fuzzerConfiguration.put("definition", definition);
	}
	
	public String toString() {
		return toString(false);
	}
	
	public String toString(boolean prettyPrint) {
		String json = fuzzerConfiguration.toJSONString();;
		if(prettyPrint) {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonParser jp = new JsonParser();
			JsonElement je = jp.parse(json);
			return gson.toJson(je);
		} else {
			return json;
		}
	}
	
}
