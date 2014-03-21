/*
 * I do hereby declare this code to be public domain. 
 * Do whatever the **** you want with it.
 * -CCHall
 */

package edu.prl.kramerlab.script;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import jdk.nashorn.api.scripting.*;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.script.ScriptContext;

// TODO: more methods
// TODO: example app
// TODO: documentation

/**
 *
 * @author CCHall
 */
public class JavascriptEngine {
	private final ScriptEngineManager manager;
	private final ScriptEngine engine;
	
	public JavascriptEngine(){
		manager = new ScriptEngineManager();
		engine = manager.getEngineByName("nashorn");
		engine.createBindings();
	}
	
	public void clearJavascriptBindings(){
		engine.getBindings(ScriptContext.ENGINE_SCOPE).clear();
		engine.getBindings(ScriptContext.GLOBAL_SCOPE).clear();
		engine.createBindings();
	}
	
	public void bindObject(String variableName, Object obj){
		engine.put(variableName, obj);
	}
	
	public void bindMethod(Method method, Object instance){
		engine.put(method.getName(), new MethodBinding(method, instance));
	}
	public void bindMethod(Object instance, String methodName, Class... paramTypes) throws NoSuchMethodException{
		Method method = instance.getClass().getMethod(methodName, paramTypes);
		engine.put(method.getName(), new MethodBinding(method, instance));
	}
	public Object getBinding(String variableName){
		return engine.get(variableName);
	}
	
	
	private static class MethodBinding implements JSObject{
		private final Object instance;
		private final Method method;
		public MethodBinding(Method m, Object target){
			this.method = m;
			this.instance = target;
		}
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("\tpost." + method.getName() + "(");
			boolean comma = false;
			for (Class p : method.getParameterTypes()) {
				if (comma) {
					sb.append(", ");
				}
				sb.append(p.getSimpleName());
				comma = true;
			}
			sb.append(")");
			return sb.toString();
		}
		@Override
		public Object call(Object o, Object... os) {
			try {
				// ignore o, it is simply a scope reference
				return method.invoke(instance, os);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
				throw new RuntimeException("Illegal Argument. Function "+this.toString()+" cannot accept arguments "+Arrays.deepToString(os),ex);
			}
		}

		@Override
		public Object newObject(Object... os) {
			throw new UnsupportedOperationException(this.toString() 
					+ " maps to a native Java method and cannot be instantiated"); 
		}

		@Override
		public Object eval(String string) {
			throw new UnsupportedOperationException(this.toString()+".eval(...) is not a function."); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public Object getMember(String string) {
			throw new UnsupportedOperationException(this.toString() 
					+ " maps to a native Java method and cannot be instantiated");
		}

		@Override
		public Object getSlot(int i) {
			throw new UnsupportedOperationException(this.toString() 
					+ " maps to a native Java method and cannot be instantiated");
		}

		@Override
		public boolean hasMember(String string) {
			return false;
		}

		@Override
		public boolean hasSlot(int i) {
			return false;
		}

		@Override
		public void removeMember(String string) {
			throw new UnsupportedOperationException(this.toString() 
					+ " maps to a native Java method and cannot be instantiated");
		}

		@Override
		public void setMember(String string, Object o) {
			throw new UnsupportedOperationException(this.toString() 
					+ " maps to a native Java method and cannot be instantiated");
		}

		@Override
		public void setSlot(int i, Object o) {
			throw new UnsupportedOperationException(this.toString() 
					+ " maps to a native Java method and cannot be instantiated");
		}

		@Override
		public Set<String> keySet() {
			return new HashSet<>();
		}

		@Override
		public Collection<Object> values() {
			return new HashSet<>();
		}

		@Override
		public boolean isInstance(Object o) {
			return false;
		}

		@Override
		public boolean isInstanceOf(Object o) {
			return false;
		}

		@Override
		public String getClassName() {
			return method.getName();
		}

		@Override
		public boolean isFunction() {
			return true;
		}

		@Override
		public boolean isStrictFunction() {
			return true;
		}

		@Override
		public boolean isArray() {
			return false;
		}

		@Override
		public double toNumber() {
			if(method.getParameterCount() == 0 && Number.class.isAssignableFrom(method.getReturnType())){
				try{return ((Number)method.invoke(instance, (Object[]) null)).doubleValue();} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
				throw new RuntimeException("Error invoking Java native function "+this.toString(),ex);
			}
			} else {
				throw new UnsupportedOperationException(this.toString() 
						+ " maps to a native Java method that does not return a number or requires parameters");
			}
		}
		
	}
}
