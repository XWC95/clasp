package com.vea.safecatcher;

import com.vea.clasp.plugin.api.Capt;
import com.vea.clasp.plugin.api.OutputProvider;
import com.vea.clasp.plugin.api.Plugin;
import com.vea.clasp.plugin.api.annotations.Def;
import com.vea.clasp.plugin.api.asm.CaptClassVisitor;
import com.vea.clasp.plugin.api.graph.ClassInfo;
import com.vea.clasp.plugin.api.graph.Status;
import com.vea.clasp.plugin.api.log.Logger;
import com.vea.clasp.plugin.api.process.AnnotationProcessor;
import com.vea.clasp.plugin.api.transform.ClassRequest;
import com.vea.clasp.plugin.api.transform.ClassTransformer;
import com.vea.clasp.plugin.api.util.RelativeDirectoryProvider;

import org.objectweb.asm.MethodVisitor;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * @author Vea
 * @version VERSION
 * @since 2019-03-25
 */
@Def()
public class TestPlugin extends Plugin<Capt> {

    @Override
    public void onCreate(Capt capt) throws IOException {
    }

    @Override
    public AnnotationProcessor onProcessAnnotations() {
        return null;
    }

    @Override
    public ClassTransformer onTransformClass() {
        return new ClassTransformer() {
            @Override
            public ClassRequest beforeTransform() {
                return new ClassRequest() {
                    @Override
                    public Set<String> extraSpecified() {
                        return super.extraSpecified();
                    }

                    @Override
                    public ClassRequest.Scope scope() {
                        return Scope.ALL;
                    }
                };
            }

            @Override
            public CaptClassVisitor onTransform(ClassInfo classInfo, boolean required) {
                if(classInfo.name().contains("MainActivity")){
                    return new SafeClassVisitor();
                }else{
                    return null;
                }
            }

            @Override
            public void afterTransform() throws IOException {
            }
        };
    }

    class SafeClassVisitor extends CaptClassVisitor {
        private String className;

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.className = name;
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
            return mv == null ? null : new TimingMethodAdapter(className + File.separator + name, access, desc, mv);
        }
    }
}
