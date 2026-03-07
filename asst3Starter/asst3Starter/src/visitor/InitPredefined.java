package visitor;

import syntaxtree.*;
import java.util.HashMap;

public class InitPredefined
{

    // returns a dummy symbol table
    static HashMap<String,ClassDecl> initEnv(ClassDeclList classes)
    {
        HashMap<String,ClassDecl> env = new HashMap<String,ClassDecl>();

        ClassDecl obj = createClass("Object", "");
        env.put("Object",  obj);
        classes.add(obj);

        ClassDecl str = createClass("String", "Object");
        str.superLink = obj;
        obj.subclasses.add(str);
        env.put("String",  str);
        classes.add(str);

        ClassDecl lib = createClass("Lib", "Object");
        lib.superLink = obj;
        obj.subclasses.add(lib);
        env.put("Lib",  lib);
        classes.add(lib);

        ClassDecl run = createClass("RunMain", "Object");
        run.superLink = obj;
        obj.subclasses.add(run);
        env.put("RunMain",  run);
        classes.add(run);

        addMethod(obj, "hashCode",    "int",     args());
        addMethod(obj, "equals",      "boolean", args("Object"));
        addMethod(obj, "toString",    "String",  args());
        addMethod(lib, "readLine",    "String",  args());
        addMethod(lib, "readInt",     "int",     args());
        addMethod(lib, "readChar",    "int",     args());
        addMethod(lib, "printStr",    "void",    args("String"));
        addMethod(lib, "printBool",   "void",    args("boolean"));
        addMethod(lib, "printInt",    "void",    args("int"));
        addMethod(lib, "intToString", "String",  args("int"));
        addMethod(lib, "intToChar",   "String",  args("int"));
        addMethod(str, "hashCode",    "int",     args());
        addMethod(str, "equals",      "boolean", args("Object"));
        addMethod(str, "toString",    "String",  args());
        addMethod(str, "concat",      "String",  args("String"));
        addMethod(str, "substring",   "String",  args("int","int"));
        addMethod(str, "length",      "int",     args());
        addMethod(str, "charAt",      "int",     args("int"));
        addMethod(str, "compareTo",   "int",     args("String"));

        superMethod(str,obj,"toString");
        superMethod(str,obj,"hashCode");
        superMethod(str,obj,"equals");

        return env;
    }

    private static void superMethod(ClassDecl t, ClassDecl s, String m)
    {
        t.methodEnv.get(m).superMethod = t.methodEnv.get(m);
    }

    private static ClassDecl createClass(String name, String superName)
    {
        return new ClassDecl(-1, name, superName, new DeclList());
    }

    private static VarDeclList args()
    {
        return new VarDeclList();
    }
    private static VarDeclList args(String type)
    {
        VarDeclList args = new VarDeclList();
        args.addElement(new ParamDecl(-1, type(type), "param0"));
        return args;
    }
    private static VarDeclList args(String t1, String t2)
    {
        VarDeclList args = new VarDeclList();
        args.addElement(new ParamDecl(-1, type(t1), "param0"));
        args.addElement(new ParamDecl(-1, type(t2), "param1"));
        return args;
    }

    private static void addMethod(ClassDecl c, String name,
                                  String retType, VarDeclList args)
    {
        StmtList sl = new StmtList();
        MethodDecl m;
        if (retType.equals("void"))
        {
            m = new MethodDeclVoid(-1, name, args, sl);
        }
        else
        {
            Type t = type(retType);
            m = new MethodDeclNonVoid(-1, t, name, args, sl, retExp(t));
        }
        m.classDecl = c;
        c.decls.addElement(m);
        c.methodEnv.put(name,m);
    }

    private static Type type(String s)
    {
        switch(s)
        {
            case "void":    return new VoidType(-1);
            case "boolean": return new BoolType(-1);
            case "int":     return new IntType(-1);
            default:        return new IDType(-1, s);
        }
    }

    private static Exp retExp(Type t)
    {
        if (t instanceof IntType)
            return new IntLit(-1,0);
        else if (t instanceof BoolType)
            return new False(-1);
        else
            return new Null(-1);
    }

}
