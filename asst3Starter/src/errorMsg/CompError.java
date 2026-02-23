package errorMsg;
import java.util.List;
import syntaxtree.Type;

/**
 * Represent an error in the compiler.
 * Use the static methods below to reate an error.
 * Don't use the CompError constructor.
 */
public class CompError extends CompMessage
{
    /**
     * Create a compilation error object.
     * DON'T USE THIS. I need to make it public for WrangLR.
     */
    public CompError(String m)
    {
        super(m);
    }

    /**
     * Represent an error as a string using the format.
     * {@code file::line.char: error message}
     */
    public String toString()
    {
        return file + "::" + line + "." + chr + ": " + message;
    }

    //////////////////////////////////////////////////////////////////////////////////
    // Lexer Errors
    //
    // Used in TokenGrammar
    //////////////////////////////////////////////////////////////////////////////////

    /**
     * Error for character literal contianing more than one character. <br/>
     * Example: {@code 'abc'}
     *
     * @param c the first character in the literal
     * @param cs the rest of the characters in the literal
     */
    public static CompError BigChar(char c, List<Character> cs)
    {
        return new CompError("found character string " + c + cs.toString());
    }

    /**
     * Error for empty character literal. <br/>
     * Example: {@code ''}
     */
    public static CompError EmptyChar()
    {
        return new CompError("found empty character");
    }

    /**
     * Error for Illegal character (usually invalid ASCII code). <br/>
     * illegal character are ASCII values 0..8, 11, 14..31, and 127..255
     */
    public static CompError IllegalChar(char c)
    {
        return new CompError("Illegal character ("+(int)c+") found.");
    }

    /**
     * Error for Integer that is not between {@code -2,147,483,648} and {@code 2,147,483,647}. <br/>
     * Example: {@code 2147483648}
     *
     * @param s the string representing the integer that's out of range.
     */
    public static CompError OutOfRange(String s)
    {
        return new CompError("Integer literal value "+s+" is out of range.");
    }

    /**
     * Error for an unterminated character. <br/>
     * Example: {@code 'x}
     */
    public static CompError UnterminatedChar()
    {
        return new CompError("unterminated character literal");
    }

    /**
     * An Unterminated Single line comment. <br/>
     * This can only happen if the comment is the very last line of the file.
     */
    public static CompError UnterminatedSingleLineComment()
    {
        return new CompError("unterminated single line comment");
    }

    /**
     * An unterminated multi line comment. <br/>
     * This can only happen if the comment goes to the end of the file.
     */
    public static CompError UnterminatedMultiLineComment()
    {
        return new CompError("unterminated multi line comment");
    }

    /**
     * An unterminated String. <br/>
     * This can conly happen if the string goes to the end of the file.
     */
    public static CompError UnterminatedString()
    {
        return new CompError("unterminated string literal");
    }

    //////////////////////////////////////////////////////////////////////////////////
    // Name Binding Errors
    //
    // Use in Sem1Visitor, Sem2Visitor, Sem3Visitor
    //////////////////////////////////////////////////////////////////////////////////

    /**
     * An error for when two classes are declared with the same name. <br/>
     * Example: <br/>
     * {@code class ThisClass { }} <br/>
     * {@code class ThisClass { }} <br/>
     *
     * @param name the name of the duplicate class.
     */
    public static CompError DuplicateClass(String name)
    {
        return new CompError("duplicate class name " + name);
    }

    /**
     * An error representing a duplicate field. <br/>
     * The field can be of a different type. <br/>
     * Example: <br/>
     * <code>
     * class ThisClass                   <br/>
     * {                                 <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;int x;    <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;double x; <br/>
     * }
     * </code>
     *
     * @param name the name of the field.
     */
    public static CompError DuplicateField(String name)
    {
        return new CompError("duplicate field name " + name);
    }

    /**
     * An error representing a duplicate method in the same class. <br/>
     * Example: <br/>
     * <code>
     * class ThisClass                                      <br/>
     * {                                                    <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;int foo() { return 0; }      <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;double foo() { return 0.0; } <br/>
     * }
     * </code>
     *
     * @param name the name of the duplicate method.
     */
    public static CompError DuplicateMethod(String name)
    {
        return new CompError("duplicate method name " + name);
    }

    /**
     * An error representing a class that is either "String" or "RunMain". <br/>
     * Example: <br/>
     * {@code class String { }}
     *
     * @param name the name of the class 
     *   (should be either {@code "String"} or {@code "RunMain"})
     */
    public static CompError IllegalSuperclass(String name)
    {
        return new CompError(name+" cannot be a superclass");
    }

    /**
     * An error for a class that extends an undefined class. <br/>
     * Example: <br/>
     * {@code class thisClass extends DoesNotExist { }}
     *
     * @param name the name of the superclass that does not exist.
     */
    public static CompError UndefinedSuperclass(String name)
    {
        return new CompError("undefined super class " + name);
    }

    /**
     * An error if an inheritance cycle is detected. <br/>
     * Example: <br/>
     * <code>
     * class A extends B { } <br/>
     * class B extends C { } <br/>
     * class C extends A { } <br/>
     * </code>
     *
     * @param name the name of the class where the inheritance cycle was discovered.
     */
    public static CompError InheritanceCycle(String name)
    {
        return new CompError("inheritance cycle detected: "+name);
    }

    /**
     * An error if a duplicate local variable is detected. <br/>
     * Example: <br/>
     * <code>
     * int x = 2;      <br/>
     * double x = 5.0; <br/>
     * </code>
     * or <br/>
     * <code>public void foo(int x, int x) { } </code>
     *
     * @param name the name of the local variable.
     */
    public static CompError DuplicateVariable(String name)
    {
        return new CompError("duplicate variable name " + name);
    }

    /**
     * An error for if length is used as a field in a class. <br/>
     * Example: <br/>
     * <code>
     * class ThisClass                        <br/>
     * {                                      <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;String length; <br/>
     * }
     * </code>
     */
    public static CompError IllegalLength()
    {
        return new CompError("Illegal name 'length' used as a field");
    }

    /**
     * An error for a break found outside of a loop or switch statement. <br/>
     * Example: <br/>
     * <code>
     * {                                          <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;break;             <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;while(x != 10) { } <br/>
     * }
     * </code>
     */
    public static CompError TopLevelBreak()
    {
        return new CompError("Break statement not in loop or switch");
    }

    /**
     * An error for an uninitialized local variable. <br/>
     * Example: <br/>
     * <code>
     * int x;     <br/>
     * int y = 5; <br/>
     * </code>
     *
     * @param name the name of the uninitialized variable.
     */
    public static CompError UninitializedVariable(String name)
    {
        return new CompError("Reference to uninitialized variable " + name);
    }

    /**
     * An error for a variable use that is not defined. <br/>
     * Example:                                  <br/>
     * {@code * int y = undefinedVariable + 5; } <br/>
     * or                                        <br/>
     * {@code int y = y + 5; }                   <br/>
     *
     * @param name the name of the undefined variable.
     */
    public static CompError UndefinedVariable(String name)
    {
        return new CompError("Undefined variable name "+name);
    }

    /**
     * An error for the use of a class that hasen't been defined. <br/>
     *
     * Example: <br/>
     * {@code UndefinedClassName x = new UndefinedClassName();}
     *
     * @param name the name of the undefined class
     */
    public static CompError UndefinedClass(String name)
    {
        return new CompError("Undefined class name "+name);
    }
    
    
    //////////////////////////////////////////////////////////////////////////////////
    // Type Checking Errors
    //////////////////////////////////////////////////////////////////////////////////
    
    /**
     * An error for when one type should be a subtype, but isn't. <br/>
     *
     * Example: <br/>
     * {@code class A { ... }           } <br/>
     * {@code class B extends A { ... } } <br/>
     * {@code class C { ... }           } <br/>
     * {@code ...                       } <br/>
     * {@code A c = new C();            } <br/>
     *
     * since {@code C} is not a subtype of {@code A}, this is not allowed.
     *
     * @param t1 the type that should be a subtype ({@code C} in our example)
     * @param t2 the type that should be a supertype ({@code A} in our example)
     */
    public static CompError Subtype(Type t1, Type t2)
    {
        return new CompError("Type-mismatch: " + t1 + " is not a subtype of " + t2);
    }

    /**
     * An error for if the left hand side of an assignment is not assignable. <br/>
     * Example: {@code 2 + 2 = x;} <br/>
     */
    public static CompError Assignment()
    {
        return new CompError("Assignment target not modifiable");
    }

    /**
     * An error for an array subscript used on an expression that isn't an array. <br/>
     * Example: {@code 4[6] = 2;} <br/>
     */
    public static CompError ArrayType()
    {
        return new CompError("Array type expected");
    }

    /**
     * An error for if two types are not compatible. <br/>
     * Two types t1 and t2 are compatible if one is a subtype of the other,
     * but the order doesn't matter.<br/>
     * <br/>
     * Example: {@code 4 == "hi"}; <br/>
     * Example: {@code int x == "hi"}; <br/>
     * Example: {@code 4 istanceof String}; <br/>
     *
     * @param t1 the type of the left hand side
     * @param t2 the type of the right hand side
     */
    public static CompError IncompatibleType(Type t1, Type t2)
    {
        return new CompError("Type-mismatch: "+ t1 + " is not compatible with " + t2);
    }

    /**
     * An error for if a method isn't defined. <br/>
     * This has to be in typechecking because we have to know the type
     * of the expression to see if the method exists. <br/>
     * <br/>
     * Example: <br/>
     * <code>
     * class Empty { }         <br/>
     * ...                     <br/>
     * Empty x = new Empty();  <br/>
     * x.doMethod();           <br/>
     * </code>
     *
     * @param methName the name of the method
     * @param t the type of the object that is supposed to have the method
     */
    public static CompError UndefinedMethod(String methName, Type t)
    {
        return new CompError("Method "+methName+" not defined for "+t);
    }

    /**
     * An error if the wrong number of arguments are used in a method call. <br/>
     * Example<br/>
     * <code>
     * class ThisClass { void foo(int a) { } }  <br/>
     * ...                                      <br/>
     * ThisClass x = new ThisClass();           <br/>
     * x.foo(1,2,3);                            <br/>
     * </code>
     *
     * @param methName the name of the method
     * @param argSize the number of arguments used in the method call
     * @param paramSize the number of parameters in the method definition
     */
    public static CompError ParameterMismatch(String methName, int argSize, int paramSize)
    {
        return new CompError("Wrong number of parameters in call to '"+
              methName+"': "+ argSize+" ("+ paramSize+" expected)");
    }

    /**
     * An error for a field access for an object where the class doesn't define the field. <br/>
     * Example: <br/>
     * <code>
     * class Empty { }         <br/>
     * ...                     <br/>
     * Empty x = new Empty();  <br/>
     * x.someField = 4;        <br/>
     * </code>
     *
     * @param fieldName the name of the field
     * @param t the type of class that is supposed to have the field.
     */
    public static CompError UndefinedField(String fieldName, Type t)
    {
        return new CompError("Field "+fieldName+" not defined for "+t);
    }

    /**
     * An error for when a type needs to be exactly int or bool. <br/>
     * Example: {@code 4 + new Ostrich();}
     */
    public static CompError TypeMismatch(Type t1, Type t2)
    {
        return new CompError("Type-mismatch: expected " + t2 + " found " + t1);
    }

    /**
     * An error for someone trying to make a new RunMain object. <br/>
     * Example: {@code new RunMain()} <br/>
     */
    public static CompError NewRunMain()
    {
        return new CompError("Cannot create object of type 'RunMain'");
    }

    /////////////////////////
    // Override errors
    /////////////////////////
    
    /**
     * An error for when the return types for overriding methods don't match. <br/>
     *
     * Example: <br/>
     * <code>
     * class A                                      <br/>
     * {                                            <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;int foo() { ... }    <br/>
     * }                                            <br/>
     * class B extends A                            <br/>
     * {                                            <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;double foo() { ... } <br/>
     * }
     * </code>
     */
    public static CompError ReturnOverride()
    {
        return new CompError("Method has different return type than superclass method.");
    }

    /**
     * An error for if an argument type in an overridden method doesn't match the base method. <br/>
     * Example: <br/>
     * <code>
     * class A                                            <br/>
     * {                                                  <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;void foo(int x) { ... }    <br/>
     * }                                                  <br/>
     * class B extends A                                  <br/>
     * {                                                  <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;void foo(double x) { ... } <br/>
     * }
     * </code>
     */
    public static CompError ArgTypeOverride()
    {
        return new CompError("Method has different argument types than superclass method.");
    }

    /**
     * An error for if the number of arguments doesn't match the number in the base method. <br/>
     * Example: <br/>
     * <code>
     * class A                                            <br/>
     * {                                                  <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;void foo(int x) { ... }    <br/>
     * }                                                  <br/>
     * class B extends A                                  <br/>
     * {                                                  <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;void foo(int x, double y) { ... } <br/>
     * }
     * </code>
     */
    public static CompError NumArgsOverride()
    {
        return new CompError("Method has different number of arguments than superclass method.");
    }

    /////////////////////////
    // Switch errors
    /////////////////////////

    /**
     * An error for if the first statement in a switch isn't a case or default. <br/>
     * Example: <br/>
     * <code>
     * switch(x) {     <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;int x = 4;  <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;case 4:     <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;break;  <br/>
     * }
     * </code>
     */
    public static CompError FirstLabelSwitch()
    {
        return new CompError("First statement of a switch must be a case or default");
    }

    /**
     * An error for if there is a statment after a break that is not a label. <br/>
     * Example: <br/>
     * <code>
     * switch(x) {     <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;case 4:     <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;break;  <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;int x = 4;  <br/>
     * }
     * </code>
     */
    public static CompError LabelAfterBreakSwitch()
    {
        return new CompError("The statement following a break must be a case or default");
    }

    /**
     * An error for if the last statement in a switch isn't a break statement. <br/>
     * Example: <br/>
     * <code>
     * switch(x) {     <br/>
     * ...             <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;default:     <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;int x = 4;  <br/>
     * }
     * </code>
     */
    public static CompError EndBreakSwitch()
    {
        return new CompError("Last statement of a switch must be a break");
    }

    /**
     * An error for if there is a duplicate default statement in a switch. <br/>
     * Example: <br/>
     * <code>
     * switch(x) {     <br/>
     * ...             <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;default:     <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;int x = 4;  <br/>
     * }
     * </code>
     */
    public static CompError DuplicateDefaultSwitch()
    {
        return new CompError("More than one default found in switch");
    }

    /**
     * An error for if there is a duplicate key in a case statement. <br/>
     * Example: <br/>
     * <code>
     * switch(x) {     <br/>
     * ...             <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;case 4:     <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;break;  <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;case 4:     <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;break;  <br/>
     * }
     * </code>
     */
    public static CompError DuplicateKeySwitch()
    {
        return new CompError("Same value found in multiple cases");
    }

    /**
     * An error for if the expression in a case statement is non-constant. <br/>
     * Example: <br/>
     * <code>
     * switch(x) {     <br/>
     * ...             <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;case x + 4:     <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;break;  <br/>
     * }
     * </code>
     */
    public static CompError NonConstantCase()
    {
        return new CompError("case labels must be constant values.");
    }
}
