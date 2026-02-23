package visitor;

import syntaxtree.*;
import java.util.*;
import errorMsg.*;

// the purpose of this class is to
// - link each ClassDecl to the ClassDecl for its superclass 
//    (via its 'superLink')
// - link each ClassDecl to each of its subclasses 
//    (via the 'subclasses' instance variable)
// - ensure that there are no cycles in the inheritance hierarchy
// - ensure that no class has 'String' or 'RunMain' as its superclass
public class Sem2Visitor extends Visitor
{

    HashMap<String,ClassDecl> classEnv;
    ErrorMsg errorMsg;

    public Sem2Visitor(HashMap<String,ClassDecl> env, ErrorMsg e)
    {
        errorMsg = e;
        classEnv = env;
    }

}
