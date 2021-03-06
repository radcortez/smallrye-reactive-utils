package io.smallrye.mutiny.vertx.codegen;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.AsyncResultUni;
import io.smallrye.mutiny.vertx.codegen.lang.CodeGenHelper;
import io.vertx.codegen.ClassModel;
import io.vertx.codegen.MethodInfo;
import io.vertx.codegen.ParamInfo;
import io.vertx.codegen.type.ClassTypeInfo;
import io.vertx.codegen.type.ParameterizedTypeInfo;
import io.vertx.codegen.type.TypeInfo;

public class MutinyGenerator extends AbstractMutinyGenerator {

    MutinyGenerator() {
        this.kinds = Collections.singleton("class");
        this.name = "mutiny";
    }

    @Override
    protected void genMethods(ClassModel model, MethodInfo method, List<String> cacheDecls, PrintWriter writer) {
        genMethod(model, method, cacheDecls, writer);
        MethodInfo publisherOverload = genOverloadedMethod(method, org.reactivestreams.Publisher.class);
        if (publisherOverload != null) {
            genMethod(model, publisherOverload, cacheDecls, writer);
        }
    }

    @Override
    protected void genForgetMethods(ClassModel model, MethodInfo method, List<String> cacheDecls,
            PrintWriter writer) {
        genForgetMethod(false, model, method, cacheDecls, writer);
    }

    @Override
    protected void genConsumerMethodInfo(boolean decl, ClassModel model, MethodInfo method, PrintWriter writer) {
        MethodInfo futMethod = genConsumerMethodInfo(method);
        startMethodTemplate(false, futMethod.getName(), futMethod, "", writer);
        if (decl) {
            writer.println(";");
            return;
        }
        writer.println(" {");
        writer.print("    ");
        if (!method.getReturnType().isVoid()) {
            writer.print("return ");
        }
        writer.print("__" + method.getName() + "(");
        List<ParamInfo> params = futMethod.getParams();
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) {
                writer.print(", ");
            }
            ParamInfo param = params.get(i);
            if (i < params.size() - 1) {
                writer.print(param.getName());
            } else {
                writer.print(param.getName() + " != null ? " + param.getName() + "::accept : null");
            }
        }
        writer.println(");");
        writer.println("  }");
        writer.println();
    }

    @Override
    protected void genUniMethod(boolean decl, ClassModel model, MethodInfo method, PrintWriter writer) {
        MethodInfo futMethod = genUniMethodInfo(method);
        ClassTypeInfo raw = futMethod.getReturnType().getRaw();
        String methodSimpleName = raw.getSimpleName();
        String adapterType = AsyncResultUni.class.getName() + ".to" + methodSimpleName;
        startMethodTemplate(false, futMethod.getName(), futMethod, "", writer);
        if (decl) {
            writer.println(";");
            return;
        }
        writer.println(" { ");
        writer.print("    return ");
        writer.print(adapterType);
        writer.println("(handler -> {");
        writer.print("      __");
        writer.print(method.getName());
        writer.print("(");
        List<ParamInfo> params = futMethod.getParams();
        writer.print(params.stream().map(ParamInfo::getName).collect(Collectors.joining(", ")));
        if (params.size() > 0) {
            writer.print(", ");
        }
        writer.println("handler);");
        writer.println("    });");
        writer.println("  }");
        writer.println();
    }

    @Override
    protected void genBlockingMethod(boolean decl, ClassModel model, MethodInfo method, PrintWriter writer) {
        MethodInfo blockingMethod = genBlockingMethodInfo(method);
        startMethodTemplate(false, blockingMethod.getName() + "AndAwait", blockingMethod, "", writer);
        if (decl) {
            writer.println(";");
            return;
        }
        writer.println(" { ");
        writer.print("    return (" + CodeGenHelper.genTypeName(blockingMethod.getReturnType()) + ") ");
        writer.print(blockingMethod.getName());
        writer.print("(");
        List<ParamInfo> params = blockingMethod.getParams();
        writer.print(params.stream().map(ParamInfo::getName).collect(Collectors.joining(", ")));
        writer.println(").await().indefinitely();");
        writer.println("  }");
        writer.println();
    }

    public MethodInfo genConsumerMethodInfo(MethodInfo method) {
        List<ParamInfo> futParams = new ArrayList<>();
        int count = 0;
        int size = method.getParams().size() - 1;
        while (count < size) {
            ParamInfo param = method.getParam(count);
            futParams.add(param);
            count = count + 1;
        }
        ParamInfo futParam = method.getParam(size);
        TypeInfo consumerType = ((ParameterizedTypeInfo) futParam.getType()).getArg(0);
        TypeInfo consumerUnresolvedType = ((ParameterizedTypeInfo) futParam.getUnresolvedType()).getArg(0);
        TypeInfo consumerReturnType = new io.vertx.codegen.type.ParameterizedTypeInfo(
                io.vertx.codegen.type.TypeReflectionFactory.create(Consumer.class).getRaw(),
                consumerUnresolvedType.isNullable(), Collections.singletonList(consumerType));
        futParams.add(new ParamInfo(futParams.size(), futParam.getName(), futParam.getDescription(),
                consumerReturnType));
        return method.copy().setParams(futParams);
    }

    private MethodInfo genUniMethodInfo(MethodInfo method) {
        List<ParamInfo> futParams = new ArrayList<>();
        int count = 0;
        int size = method.getParams().size() - 1;
        while (count < size) {
            ParamInfo param = method.getParam(count);
            futParams.add(param);
            count = count + 1;
        }
        ParamInfo futParam = method.getParam(size);
        TypeInfo futType = ((ParameterizedTypeInfo) ((ParameterizedTypeInfo) futParam.getType()).getArg(0)).getArg(0);
        TypeInfo futUnresolvedType = ((ParameterizedTypeInfo) ((ParameterizedTypeInfo) futParam.getUnresolvedType())
                .getArg(0))
                        .getArg(0);
        TypeInfo futReturnType = new io.vertx.codegen.type.ParameterizedTypeInfo(
                io.vertx.codegen.type.TypeReflectionFactory.create(Uni.class).getRaw(),
                futUnresolvedType.isNullable(), Collections.singletonList(futType));
        return method.copy().setReturnType(futReturnType).setParams(futParams);
    }

    private MethodInfo genBlockingMethodInfo(MethodInfo method) {
        List<ParamInfo> futParams = new ArrayList<>();
        int count = 0;
        int size = method.getParams().size() - 1;
        while (count < size) {
            ParamInfo param = method.getParam(count);
            futParams.add(param);
            count = count + 1;
        }
        ParamInfo futParam = method.getParam(size);
        TypeInfo futType = ((ParameterizedTypeInfo) ((ParameterizedTypeInfo) futParam.getType()).getArg(0)).getArg(0);
        TypeInfo futReturnType = futType;
        return method.copy().setReturnType(futReturnType).setParams(futParams);
    }

    private MethodInfo genOverloadedMethod(MethodInfo method, Class streamType) {
        List<ParamInfo> params = null;
        int count = 0;
        for (ParamInfo param : method.getParams()) {
            if (param.getType().isParameterized()
                    && param.getType().getRaw().getName().equals("io.vertx.core.streams.ReadStream")) {
                if (params == null) {
                    params = new ArrayList<>(method.getParams());
                }
                ParameterizedTypeInfo paramType = new io.vertx.codegen.type.ParameterizedTypeInfo(
                        io.vertx.codegen.type.TypeReflectionFactory.create(streamType).getRaw(),
                        false,
                        Collections.singletonList(((ParameterizedTypeInfo) param.getType()).getArg(0)));
                params.set(count, new io.vertx.codegen.ParamInfo(
                        param.getIndex(),
                        param.getName(),
                        param.getDescription(),
                        paramType));
            }
            count = count + 1;
        }
        if (params != null) {
            return method.copy().setParams(params);
        }
        return null;
    }
}
