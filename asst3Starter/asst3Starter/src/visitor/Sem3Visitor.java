package visitor;

import errorMsg.*;
import java.util.*;
import syntaxtree.*;
// The purpose of this class is to:
// - link each variable reference to its corresponding VarDecl
//    (via its 'link' field)
//   - undefined variable names are reported
// - link each type reference to its corresponding ClassDecl
//   - undefined type names are reported
// - link each Break expression to its enclosing While or Case statement
//   - a break that is not inside any while loop or case is reported
// - report conflicting local variable names (including formal parameter names)
// - ensure that no instance variable has the name 'length'
public class Sem3Visitor extends Visitor
{
    // current class we're visiting
    ClassDecl currentClass;

    // environment for names of classes
    HashMap<String, ClassDecl> classEnv;

    // environment for names of variables
    HashMap<String, VarDecl> localEnv;

    // set of initialized variables
    HashSet<String> init;

    // set of unused classes
    HashSet<String> unusedClasses;

    // set of unused local variables
    // We use a hashmap so we can store the position where we found it for the error message.
    HashMap<String,Integer> unusedLocals;

    // stack of while/switch
    Stack<BreakTarget> breakTargetStack;

    //error message object
    ErrorMsg errorMsg;

    // constructor
    public Sem3Visitor(HashMap<String,ClassDecl> env, ErrorMsg e)
    {
        errorMsg         = e;
        currentClass     = null;
        classEnv         = env;
        localEnv         = new HashMap<String,VarDecl>();
        breakTargetStack = new Stack<BreakTarget>();
    }

    @Override
    public Object visit(ClassDecl n){
        // track the current class we're inside
        ClassDecl saved = currentClass;
        currentClass = n;
        
        // visit all fields and methods inside this class
        n.decls.accept(this);

        // restore previous class context when leaving
        currentClass = saved;
        return null;
    }

    @Override
    public Object visit(MethodDecl n){
        // save outer scope and create a fresh local environment for this method
        HashMap<String,VarDecl> savedEnv = localEnv;
        localEnv = new HashMap<>();

        // load all fields from this class and its superclasses
        ClassDecl c = currentClass;
        ArrayList<ClassDecl> chain = new ArrayList<>();
        while (c != null){
            chain.add(0, c);
            c = c.superLink;
        }

        // load all fields from the inheritance chain into local scope
        for(ClassDecl cls : chain){
            for(FieldDecl f : cls.fieldEnv.values()){
                localEnv.put(f.name, f);
            }
        }

        // add parameters to local scope
        for(Object obj : n.params){
            ParamDecl p = (ParamDecl) obj;
            VarDecl existing = localEnv.get(p.name);
            // check for duplicates
            if (existing != null && !(existing instanceof FieldDecl)){
                errorMsg.error(p.pos, CompError.DuplicateVariable(p.name));
            }
            else{
                localEnv.put(p.name, p);
            }
        }
        // visit all statements in method body
        n.stmts.accept(this);

        // restore the outer environment
        localEnv = savedEnv;
        return null;
    }
    
    @Override
public Object visit(MethodDeclNonVoid n)
{
    n.rtnType.accept(this);

     // save the outer scope 
    HashMap<String,VarDecl> savedEnv = localEnv;
    localEnv = new HashMap<>();

    // loading fields into localEnv so subclass fields shadow superclass fields
    ClassDecl c = currentClass;
    ArrayList<ClassDecl> chain = new ArrayList<>();
    while (c != null) { chain.add(0, c); c = c.superLink; }
    for (ClassDecl cls : chain)
        for (FieldDecl f : cls.fieldEnv.values())
            localEnv.put(f.name, f);

     // add each formal parameter to scope
    for (Object obj : n.params)
    {
        ParamDecl p = (ParamDecl) obj;
        VarDecl existing = localEnv.get(p.name);
        if (existing != null && !(existing instanceof FieldDecl))
            errorMsg.error(p.pos, CompError.DuplicateVariable(p.name));
        else
            localEnv.put(p.name, p);
    }

    n.stmts.accept(this);
    n.rtnExp.accept(this);  

    localEnv = savedEnv;
    return null;
}

    @Override
    public Object visit(IDType n){
         // look up the class name in the global class environment
        ClassDecl classD = classEnv.get(n.name);
        if(classD == null){
            // the type name was never declared, error message
            errorMsg.error(n.pos, CompError.UndefinedClass(n.name));
        }
        else{
            n.link = classD;
        }
        return null;
    }

    @Override
    public Object visit(IDExp n){
        // look up the variable name in the current local scope
        VarDecl varD = localEnv.get(n.name);
        if(varD == null){
             // the variable was never declared in any reachable scope, error message
            errorMsg.error(n.pos, CompError.UndefinedVariable(n.name));
        }
        else{
            n.link = varD;
        }
        return null;
    }

    @Override
    public Object visit(LocalVarDecl n){
        // visit initializer first 
        n.initExp.accept(this);
        n.type.accept(this);

        if (localEnv.containsKey(n.name)){
            VarDecl existing = localEnv.get(n.name);
            // only a duplicate error if it conflicts with a param or another local
            if (existing instanceof FieldDecl){
                localEnv.put(n.name, n); // shadow the field, so no error
            }
            else{
                errorMsg.error(n.pos, CompError.DuplicateVariable(n.name));
            }
        }
        else{
            localEnv.put(n.name, n);
        }
        return null;
    }

    // while loop
    @Override
    public Object visit(While n){
        n.exp.accept(this);
        breakTargetStack.push(n);
        n.body.accept(this);
        breakTargetStack.pop();
        return null;
    }

    // switch statement 
    @Override
    public Object visit(Switch n){
        n.exp.accept(this);
        breakTargetStack.push(n);
        n.stmts.accept(this);
        breakTargetStack.pop();
        return null;
    }

    // break statement
    @Override
    public Object visit(Break n){
        if(breakTargetStack.isEmpty()){
            errorMsg.error(n.pos, CompError.TopLevelBreak());
        }
        else{
            n.breakLink = breakTargetStack.peek();
        }
        return null;
    }

    @Override
    public Object visit(Case n){
        // go through stack to find nearest switch
        for(int i = breakTargetStack.size() - 1; i >= 0; i--){
            if(breakTargetStack.get(i) instanceof Switch s){
                n.enclosingSwitch = s;
                break;
            }
        }
        n.exp.accept(this);
        return null;
    }

    @Override
    public Object visit(Default n){
        // go through stack to find nearest switch
        for(int i = breakTargetStack.size() - 1; i >= 0; i--){
            if(breakTargetStack.get(i) instanceof Switch s){
                n.enclosingSwitch = s;
                break;
            }
        }
        return null;
    }
}
