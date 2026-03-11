package visitor;

import errorMsg.*;
import java.util.*;
import syntaxtree.*;

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

    @Override
    public Object visit(Program p){
        // link every class to its superclass
        for(ClassDecl n : p.classDecls){
            visit(n);
        }

        // cycle detection over same list
        for(ClassDecl n : p.classDecls){
            detectCycle(n);
        }

        return null;
    }

    @Override
    public Object visit(ClassDecl n){
        // skip class that have no superclass
        if(n.superName.equals("")){
            return null;
        }

        // check for illegal superclass names
        if(n.superName.equals("String")|| n.superName.equals("RunMain")){
            errorMsg.error(n.pos, CompError.IllegalSuperclass(n.superName));
            return null;
        }

        //look up superclass by name
        ClassDecl superClass = classEnv.get(n.superName);
        if(superClass == null){
            errorMsg.error(n.pos, CompError.UndefinedSuperclass(n.superName));
            return null;
        }

        n.superLink = superClass;
        superClass.subclasses.add(n);

        return null;
    }

    private void detectCycle(ClassDecl n){
        HashSet<ClassDecl> visited = new HashSet<>();
        ClassDecl curr = n;

        while(curr != null && !curr.superName.equals("")){
            // if seen this node before, found cycle
            if(visited.contains(curr)){
                errorMsg.error(n.pos, CompError.InheritanceCycle(n.name));
                return;
            }
            visited.add(curr);
            curr = curr.superLink;
        }
    }

}
