package errorMsg;

/**
 * Represents a warning message in the compiler.
 * Use the static methods to create a warning.
 */
public class CompWarning extends CompMessage
{
    private CompWarning(String m)
    {
        super(m);
    }

    public String toString()
    {
        return file + "::" + line + "." + chr + "(warning): " + message;
    }

    ////////////////////////////////////////////////////
    // Lexical Analysis
    ////////////////////////////////////////////////////
    
    /**
     * A warning if there is a comment in comment. <br/>
     * Example: <br/>
     * {@literal /} {@literal*} This is a
     * {@literal /} {@literal *} nested comment {@literal *} {@literal/}
     */
    public static CompWarning CommentInComment()
    {
        return new CompWarning("found /* inside multi-line comment");
    }

    ////////////////////////////////////////////////////
    // Name Binding
    // Sem1Visitor, Sem2Visitor, Sem3Visitor
    ////////////////////////////////////////////////////
    
    /**
     * A warning for if a class was declared but never used. <br/>
     * Example: <br/>
     * <code>
     * class A { }  <br/>
     * class Main   <br/>
     * {  <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;public void main() { }  <br/>
     * }  <br/>
     * </code>
     *
     * @param name the name of the unused class.
     */
    public static CompWarning UnusedClass(String name)
    {
        return new CompWarning("unused class "+name);
    }

    /**
     * A warning for a local variable that is declared but never used. <br/>
     * Example: <br/>
     * <code>
     * public void main() <br/>
     * { <br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;int x = 4; <br/>
     * } <br/>
     * </code>
     *
     * @param name the name of the unused variable
     */
    public static CompWarning UnusedVariable(String name)
    {
        return new CompWarning("unused variable "+name);
    }

    ////////////////////////////////////////////////////
    // Type Checking
    // Sem4Visitor, Sem5Visitor
    ////////////////////////////////////////////////////
    
    /**
     * An warning for if an array index is known to be negative at compile time. <br/>
     * Example: {@code array[-5] = 4;}
     */
    public static CompWarning NegativeIndex()
    {
        return new CompWarning("Array index cannot be negative");
    }

    /**
     * A warning for if an array was created with a negative length. <br/>
     * Example: {@code int[] array = new int[-5];}
     */
    public static CompWarning NegativeLength()
    {
        return new CompWarning("Array length cannot be negative");
    }

    /**
     * A warning for when there is unreachable code.
     */
    public static CompWarning UnreachableCode()
    {
        return new CompWarning("Unreachable code");
    }
}
