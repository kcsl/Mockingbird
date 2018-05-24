package com.kcsl.fuzzing.mockingbird.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.java.commons.analysis.CallSiteAnalysis;
import com.kcsl.fuzzing.mockingbird.analysis.MethodAnalysis.Parameter;
import com.kcsl.fuzzing.mockingbird.analysis.MethodAnalysis.Return;

public class MockRequest {

	private Node clazz;
	private List<FieldMockRequest> fieldMockRequests = new ArrayList<FieldMockRequest>();
	private List<MethodMockRequest> methodMockRequests = new ArrayList<MethodMockRequest>();
	
	public MockRequest(Q methods) {
		// TODO: impelement
	}
	
//	public MockRequest(Q clazz) {
//		this(clazz.nodes(XCSG.Classifier).eval().nodes().one());
//	}
	
	public MockRequest(Node clazz) {
		if(clazz == null || !(clazz.taggedWith(XCSG.Classifier))) {
			throw new IllegalArgumentException("Input must be a class.");
		}
		
		this.clazz = clazz;
		
		// add all the immediate methods in the class to the mock request as preserved methods (assumign they are concrete)
		for(Node method : Common.toQ(clazz).children().nodes(XCSG.Method).eval().nodes()) {
			methodMockRequests.add(new MethodMockRequest(method, true));
		}
		
//		Q typeLineage = Common.universe().edges(XCSG.Supertype).forward(Common.toQ(clazz));
//		Node current = clazz;
//		while(current != null) {
//			boolean isTargetClass = current.equals(clazz);
//			fieldMockRequests.addAll(getAccessibleFields(current, isTargetClass));
//			
//			current = typeLineage.successors(Common.toQ(current)).eval().nodes().one();
//		}
//		
////		for(Node field : ) {
////			
////		}
		
	}
	
	@Override
	public String toString() {
		return "[methods=" + methodMockRequests.toString() + ", fields=TODO]";
	}
	
	private List<FieldMockRequest> getAccessibleFields(Node current, boolean isTargetClass) {
		// TODO Auto-generated method stub
		return null;
	}

	private class FieldMockRequest {
		private boolean preserve;
		private Node field;
	}
	
	/**
	 * Constructs a field mock request 
	 * 
	 * If the class is the target class then private static and instance 
	 * fields are included in addition to protected and public instance fields
	 * 
	 * @param clazz
	 * @param isTargetClass
	 * @return
	 */
	private List<MethodMockRequest> getAccessibleMethods(Node clazz, boolean isTargetClass) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private class MethodMockRequest {
		private boolean preserve;
		private Node method;
		
		public MethodMockRequest(Node method, boolean preserve) {
			if(!(method.taggedWith(XCSG.Method))) {
				throw new IllegalArgumentException("Input must be a method.");
			}
			
			this.method = method;
			this.preserve = preserve;
			
			// if the method is not concrete it must be mocked
			if(MethodAnalysis.isAbstract(method)) {
				preserve = false;
			}
		}
		
		public Node getMethod() {
			return method;
		}
		
		public boolean shouldPreserve() {
			return preserve;
		}
		
		public String getName() {
			return MethodAnalysis.getName(method);
		}
		
		public Integer[] getModifiers() {
			return MethodAnalysis.getModifiers(method);
		}
		
		public Node getOwnerClass() {
			return MethodAnalysis.getOwnerClass(method);
		}
		
		public Return getReturnType() {
			return MethodAnalysis.getReturnType(method);
		}
		
		public Parameter[] getParameters() {
			return MethodAnalysis.getParameters(method);
		}
		
		public AtlasSet<Node> getInvocationTargets(){
			AtlasSet<Node> invocationTargets = new AtlasHashSet<Node>();
			for(Node callsite : CommonQueries.localDeclarations(Common.toQ(method)).nodes(XCSG.CallSite).eval().nodes()) {
				AtlasSet<Node> targetMethods = CallSiteAnalysis.getTargetMethods(callsite);
				invocationTargets.addAll(targetMethods);
			}
			return invocationTargets;
		}
		
		@Override
		public String toString() {
			return "[preserve=" + shouldPreserve() + ", method=" + Arrays.toString(getModifiers()) + " " + ClassAnalysis.getName(getOwnerClass()) + "." + getName() + "(" + Arrays.toString(getParameters()).replace("[","").replace("]","") + ") : " + getReturnType().toString();
		}
	}
	
}
