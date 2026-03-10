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

        // reset initialized varible tracking
        init = new HashSet<String>();

        // params are always initialized 
        for(Object obj : n.params){
            ParamDecl p = (ParamDecl) obj;
            init.add(p.name);
        }
        
        
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
            //  only check locals and params
            if(!(varD instanceof FieldDecl) && !init.contains(n.name)){
                errorMsg.error(n.pos, CompError.UninitializedVariable(n.name));
            }
            else{
                n.link = varD;
            }
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
            init.add(n.name);
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

        // first statement must be a label
        if (n.stmts.isEmpty() || !(n.stmts.get(0) instanceof Label)){
            errorMsg.error(n.pos, CompError.FirstLabelSwitch());
        }

        // last statement must be a break
        if (n.stmts.isEmpty() || !(n.stmts.get(n.stmts.size() - 1) instanceof Break)){
            errorMsg.error(n.pos, CompError.EndBreakSwitch());
        }

        boolean seenDefault  = false;
        boolean prevWasBreak = false;
        HashSet<Integer> seenKeys = new HashSet<>();

        for (int i = 0; i < n.stmts.size(); i++)
        {
            Stmt s = n.stmts.get(i);

            if (s instanceof Default){
                if (seenDefault){
                    errorMsg.error(s.pos, CompError.DuplicateDefaultSwitch());
                }
                seenDefault  = true;
                prevWasBreak = false;
            }
            else if (s instanceof Case c){
                // case expression must be a constant
                if (!(c.exp instanceof IntLit)){
                    errorMsg.error(c.pos, CompError.NonConstantCase());
                }
                else
                {
                    // no duplicate case values
                    int val = ((IntLit) c.exp).val;
                    if (seenKeys.contains(val)){
                        errorMsg.error(c.pos, CompError.DuplicateKeySwitch());
                    }
                    else{
                        seenKeys.add(val);
                    }
                }
                prevWasBreak = false;
            }
            else if (s instanceof Break){
                prevWasBreak = true;
            }
            else{
                prevWasBreak = false;
            }
        }

        // track variables declared in the current chunk
        ArrayList<String> chunkVars = new ArrayList<>();

        for (int i = 0; i < n.stmts.size(); i++)
        {
            Stmt s = n.stmts.get(i);

            if (s instanceof Break)
            {
                // end of chunk, remove all variables declared in this chunk
                for (String varName : chunkVars)
                {
                    localEnv.remove(varName);
                }
                chunkVars.clear();

                // still visit the break for break link
                s.accept(this);
            }
            else if (s instanceof LocalDeclStmt lds)
            {
                // visit the declaration for name resolution
                s.accept(this);

                // track this variable as belonging to the current chunk
                chunkVars.add(lds.localVarDecl.name);
            }
            else
            {
                // visit normally for name resolution
                s.accept(this);
            }
        }

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