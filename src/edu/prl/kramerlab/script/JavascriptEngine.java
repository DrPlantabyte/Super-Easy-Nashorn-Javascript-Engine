/*
 * I do hereby declare this code to be public domain. 
 * Do whatever the **** you want with it.
 * -CCHall
 */

package edu.prl.kramerlab.script;

import java.lang.reflect.*;
import java.util.*;
import java.util.Map.Entry;
import javax.script.*;
import jdk.nashorn.api.scripting.*;


/**
 * The JavascriptEngine creates and manages a Nashorn javascript engine. It 
 * provides methods for adding and removing objects and methods to the script 
 * environment. All you have to do is create the engine and then bind any 
 * objects or methods that you want to have available to the script. You can 
 * call functions defined in the script as well. 
 * @author CCHall
 */
public class JavascriptEngine {
	/** The version string for this library */
	public static final String VERSION = "1.0.1";
	
	private final ScriptEngineManager manager;
	private final ScriptEngine engine;
	/**
	 * Default constructor for the JavascriptEngine. It initializes the Nashorn 
	 * Javascript engine to it's default script environment.
	 */
	public JavascriptEngine(){
		manager = new ScriptEngineManager();
		engine = manager.getEngineByName("nashorn");
		engine.createBindings();
	}
	/**
	 * Deletes all variables and resets the script environment back to the 
	 * default settings.
	 */
	public void clearJavascriptBindings(){
		engine.getBindings(ScriptContext.ENGINE_SCOPE).clear();
		engine.getBindings(ScriptContext.GLOBAL_SCOPE).clear();
		engine.createBindings();
	}
	/**
	 * Adds a java object as a variable in the script environment. Note that 
	 * Javascript objects and Java objects are interconvertable but are <b>not 
	 * equivalent</b>. For example, if you bind a <code>String</code>, and then 
	 * the script performs a concatonation with that string, the bound object 
	 * will no longer be an instance of <code>java.lang.String</code>.
	 * @param variableName The name of the variable
	 * @param obj The object to bind to teh scripting environment.
	 */
	public void bindObject(String variableName, Object obj){
		getBindings().put(variableName, obj);
	}
	/**
	 * Adds a java object as a variable in the script environment, but only if 
	 * there isn't something already bound to that variable name. Note that 
	 * Javascript objects and Java objects are interconvertable but are <b>not 
	 * equivalent</b>. For example, if you bind a <code>String</code>, and then 
	 * the script performs a concatonation with that string, the bound object 
	 * will no longer be an instance of <code>java.lang.String</code>.
	 * @param variableName The name of the variable
	 * @param obj The object to bind to teh scripting environment.
	 * @return Returns the object bound. If <code>variableName</code> was 
	 * already bound to an object, then that object is returned. Otherwise, 
	 * <code>obj</code> is returned.
	 */
	public Object getOrSetObjectBinding(String variableName, Object obj){
		if (isBound(variableName) == false) {
			engine.put(variableName, obj);
			return obj;
		}
		return getBindings().get(variableName);
	}
	/**
	 * Adds a method of a Java object to appear as a global function in the 
	 * script. Note that Javascript objects and Java objects are 
	 * interconvertable but are <b>not equivalent</b>. This means that any 
	 * method you bind should take <code>Object</code>s as parameters instead of 
	 * <code>String</code> and <code>Number</code>s instead of primitive types. <p/>
	 * Function overloading is not supported in Javascript.
	 * @param method The Method to bind to the script environment
	 * @param instance The object instance that is providing the method
	 */
	public void bindMethod(Method method, Object instance){
		getBindings().put(method.getName(), new MethodBinding(method, instance));
	}
	/**
	 * Adds a method of a Java object to appear as a global function in the 
	 * script. Note that Javascript objects and Java objects are 
	 * interconvertable but are <b>not equivalent</b>. This means that any 
	 * method you bind should take <code>Object</code>s as parameters instead of 
	 * <code>String</code> and <code>Number</code>s instead of primitive types. 
	 * @param instance The object instance that is providing the method 
	 * @param methodName The name of the method to bind (must be a member of the 
	 * object you passed as the <code>instance</code> parameter.
	 * @param paramTypes A list of <code>Class</code> objects corresponding to 
	 * the parameters of the specific method you wish to bind (function 
	 * overloading is not supported in Javascript).
	 * @throws NoSuchMethodException Thrown is <code>instance</code> has no 
	 * method of name <code>methodName</code> whose parameter list is defined by 
	 * <code>paramTypes</code>.
	 */
	public void bindMethod(Object instance, String methodName, Class... paramTypes) throws NoSuchMethodException{
		Method method = instance.getClass().getMethod(methodName, paramTypes);
		getBindings().put(method.getName(), new MethodBinding(method, instance));
	}
	/**
	 * Gets a variable that has been bound to the script environment or was 
	 * assigned a value within the script. Note that Javascript objects and Java 
	 * objects are interconvertable but are <b>not equivalent</b>. For example, 
	 * if you bind a <code>String</code>, and then the script performs a 
	 * concatonation with that string, the bound object will no longer be an 
	 * instance of <code>java.lang.String</code>.
	 * @param variableName The name of the variable you want to retrieve.
	 * @return The value of the variable, or null if it is not bound/defined (or 
	 * happens to be null).
	 */
	public Object getBinding(String variableName){
		return getBindings().get(variableName);
	}
	/**
	 * Executes the provided Javascript script. A value will be returned if the 
	 * script returns something (e.g. a function call 
	 * <code>Object retval = javascriptEngine.eval("myFunc();")</code>). Note 
	 * that Javascript objects and Java objects are interconvertable but are 
	 * <b>not equivalent</b>. For example, invoking 
	 * <code>Object retval = javascriptEngine.eval("function getResult(){return 'hits='+43;}; getResult();")</code> 
	 * will <b>not</b> return an instance of <code>java.lang.String</code>.
	 * @param javascript The script to execute
	 * @return The value returned by the script (if any) or null.
	 * @throws ScriptException Thrown if there's an error in the script. The 
	 * exception message typically includes the line and column position of the 
	 * encountered error.
	 */
	public Object eval(String javascript) throws ScriptException{
		return engine.eval(javascript);
	}
	/**
	 * Determines whether a given variable name has been bound to the script 
	 * environment. Any object or method bound to the script with a 
	 * <code>bind____(...)</code> method or is declared by the script itself is 
	 * considered to be bound and can be retrieved with the 
	 * <code>getBinding(variableName)</code> method.
	 * @param variableName The name of the variable to test.
	 * @return True if the variable has been bound to a value in the script 
	 * environment, false if that variable does not exist in the script 
	 * environment.
	 */
	public boolean isBound(String variableName){
		return getBindings().containsKey(variableName);
	}
	public boolean removeBinding(String variableName){
		boolean bound = getBindings().containsKey(variableName);
		if(bound)getBindings().remove(variableName);
		return bound;
	}
	public Map<String,Object> getAllBindings(){
		Map<String,Object> map = new HashMap<>(getBindings().size());
		for(Entry<String,Object> binding : getBindings().entrySet()){
			map.put(binding.getKey(), binding.getValue());
		}
		return Collections.unmodifiableMap(map);
	}
	
	protected Bindings getBindings(){
		return engine.getBindings(ScriptContext.ENGINE_SCOPE);
	}
	/**
	 * Calls a top-level function from the script. If the function is a member 
	 * of an object, use <code>callObjectMethod(...)</code> instead.
	 * @param function The name of the function to call
	 * @param parameters The parameters (if any) to pass as arguments to the 
	 * function
	 * @return Returns the return value of the function, or null if it returns 
	 * nothing (or returns a null).
	 * @throws NoSuchMethodException Thrown if the method or its parent object 
	 * does not exist.
	 * @throws ScriptException Thrown if the script itself errors during 
	 * execution
	 */
	public Object callFunction(String function, Object... parameters) throws NoSuchMethodException, ScriptException{
		return ((Invocable)engine).invokeFunction(function, parameters);
	}
	
	/**
	 * Calls a function that is a member of an object in the script. The 
	 * invocation will look like the object path just before the function 
	 * parameters. For example, consider the following script:<p/><code>
	 * var foo = {<br/>
	 * &nbsp;bar : function(msg){<br/>
	 * &nbsp;&nbsp;print(msg);<br/>
	 * &nbsp;},<br/>
	 * &nbsp;faq : {<br/>
	 * &nbsp;&nbsp;getAnswer : function(){<br/>
	 * &nbsp;&nbsp;&nbsp;return "42";<br/>
	 * &nbsp;&nbsp;}<br/>
	 * &nbsp;}<br/>
	 * }<br/>
	 * </code><p/>
	 * If you wanted to call <code>foo.bar("Hello World")</code>, then you would 
	 * invoke this method like this:<br/><code>
	 * javascriptEngine.callObjectMethod("foo.bar","Hello World");<br/></code>
	 * To call <code>foo.faq.getAnswer()</code>, you would invoke this method 
	 * like this:<br/><code>
	 * Object answer = javascriptEngine.callObjectMethod("foo.faq.getAnswer");<br/></code>
	 * <p/>
	 * If this is too complicated, you can always <code>.eval</code> a function call:<br/><code>
	 * javascriptEngine.eval("foo.bar('Hello World')");<br/>
	 * Object answer = javascriptEngine.eval("foo.faq.getAnswer()");
	 * </code>
	 * @param methodInvocation A string representing the function path in the 
	 * form <code>"object.function"</code>. For nested objects, it will look like 
	 * <code>"object.member1.member2.function"</code>
	 * @param parameters The parameters you want to pass into the function (if 
	 * any).
	 * @return Returns the return value of the function, or null if it returns 
	 * nothing (or returns a null).
	 * @throws NoSuchMethodException Thrown if the method or its parent object 
	 * does not exist.
	 * @throws ScriptException Thrown if the script itself errors during 
	 * execution
	 */
	public Object callObjectMethod(String methodInvocation, Object... parameters) throws NoSuchMethodException, ScriptException{
		String[] callStack = methodInvocation.split("\\.");
		Object prevBinding = null;
		Object binding = getBindings().get(callStack[0]);
		for(int i = 1; i < callStack.length; i++){
			if(binding instanceof JSObject){
				prevBinding = binding;
				binding = ((JSObject)binding).getMember(callStack[i]);
			} else {
				throw new NoSuchMethodException("Object " + methodInvocation.substring(0,methodInvocation.lastIndexOf(".")) + " does not exist.");
			}
		}
		return ((JSObject)binding).call( prevBinding, parameters);
	}
	/**
	 * Attempts to read a variable in the script as a number. If the text value 
	 * of a variable is not a number, then this method will throw an exception. 
	 * If the variable is undefined, <code>NaN</code> is returned.
	 * @param variableName The name of the variable to retrieve
	 * @return The numeric value of a variable, or <code>NaN</code> if the 
	 * variable does not exist.
	 * @throws NumberFormatException 
	 */
	public double getAsNumber(String variableName) throws NumberFormatException {
		Object var = getBinding(variableName);
		if(var == null){
			// binding does not exist, return NaN
			return Double.NaN;
		}
		if(var instanceof Double){
			return ((Double)var);
		} else if(var instanceof Number){
			return ((Number)var).doubleValue();
		} else {
			String str = var.toString();
			return Double.parseDouble(str);
		}
	}
	
	/**
	 * Class used to bind methods. This is playing with the internals of the 
	 * Nashorn engine, so be careful.
	 */
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
			sb.append("post.").append(method.getName()).append("(");
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
