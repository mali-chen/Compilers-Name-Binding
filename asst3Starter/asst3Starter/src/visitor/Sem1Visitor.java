package visitor;

import errorMsg.*;
import java.util.*;
import syntaxtree.*;
// The purpose of the Sem1Visitor class is to:
// - enter each class declaration into the global environment
//   - duplicate class names are detected
// - enter each method declaration into the method environment for its class
//   - duplicate method names for a class are detected
// - enter each field declaration into the field environment for its class
//   - duplicate instance variable names for a class are detected
// - all of the above are also done for the predefined classes
//   (Object, String, Lib and RunMain)
public class Sem1Visitor extends Visitor
{

    HashMap<String,ClassDecl> classEnv;
    ClassDecl currentClass;
    ClassDeclList classes;
    ErrorMsg errorMsg;

    public Sem1Visitor(ErrorMsg e)
    {
        errorMsg     = e;
        currentClass = null;
        classes      = new ClassDeclList();
        classEnv     = InitPredefined.initEnv(classes);
    }

    public HashMap<String,ClassDecl> getEnv()
    {
        return classEnv;
    }

    /**
     * Inserts the predefined class declarations into the Program
     * object so that they can be displayed, and so that the Sem1-,
     * Sem2-, and Sem3-visitors will traverse them.
     *
     * **** CS 358 Students should NOT modify this method ****
     */
    @Override
    public Object visit(Program p)
    {
        p.setPredefined(classes);
        p.classDecls.accept(this);
        return null;
    }

    @Override 
    public Object visit(ClassDecl n){
        // check for duplicate 
        if(classEnv.containsKey(n.name)){
            errorMsg.error(n.pos, CompError.DuplicateClass(n.name));
        }
        // register class name in gloabl enviroment
        else{
            classEnv.put(n.name, n);
        }

        ClassDecl savedClass = currentClass;
        currentClass = n;
        n.decls.accept(this);
        currentClass = savedClass;

        return null;
    }

    @Override
    public Object visit(FieldDecl n){
        // "length" is reserved
        if(n.name.equals("length")){
            errorMsg.error(n.pos, CompError.IllegalLength());
        }
        // check for duplicate
        else if(currentClass.fieldEnv.containsKey(n.name)){
            errorMsg.error(n.pos, CompError.DuplicateField(n.name));
        }
        // register class name in gloabl enviroment
        else{
            currentClass.fieldEnv.put(n.name, n);
        }

        return null;
    }

    @Override
    public Object visit(MethodDecl n){
    // check for duplicate
    if(currentClass.methodEnv.containsKey(n.name)){
        errorMsg.error(n.pos, CompError.DuplicateMethod(n.name));
    }
    // register class name in gloabl enviroment
    else{
        n.classDecl = currentClass;
        currentClass.methodEnv.put(n.name, n);
    }

    return null;
    }

}
