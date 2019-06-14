package com.vea.clasp.plugin.process.plugin;

import com.google.common.collect.Sets;
import com.vea.clasp.plugin.api.Arguments;
import com.vea.clasp.plugin.api.CaptInternal;
import com.vea.clasp.plugin.api.OutputProvider;
import com.vea.clasp.plugin.api.Plugin;
import com.vea.clasp.plugin.api.asm.CaptClassVisitor;
import com.vea.clasp.plugin.api.graph.ClassInfo;
import com.vea.clasp.plugin.api.process.AnnotationProcessor;
import com.vea.clasp.plugin.api.transform.ClassRequest;
import com.vea.clasp.plugin.api.transform.ClassTransformer;
import com.vea.clasp.plugin.process.PluginBean;
import com.vea.clasp.plugin.process.visitors.AnnotationClassDispatcher;
import com.vea.clasp.plugin.process.visitors.ThirdRound;
import com.vea.clasp.plugin.resource.VariantResource;
import com.vea.clasp.plugin.util.ConcurrentHashSet;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.annotation.Nullable;

@SuppressWarnings("unchecked")
public class PluginWrapper extends ForwardingCapt {

    private final boolean incremental;
    private final Plugin plugin;
    private final Arguments args;
    private final String id;
    private final VariantResource resource;
    private final Set<String> affected = new ConcurrentHashSet<>();
    private Set<String> preAffected = Collections.emptySet();

    public PluginWrapper(boolean incremental, Plugin plugin,
                         Arguments args,
                         String id, VariantResource resource, CaptInternal delegate) {
        super(delegate);
        this.incremental = incremental;
        this.plugin = plugin;
        this.args = args;
        this.id = id;
        this.resource = resource;
    }

    public String id() {
        return id;
    }

    public void callBeforeCreate() {
        plugin.beforeCreate(this);
    }

    public void callOnCreate() throws IOException, InterruptedException {
        plugin.onCreate(this);
    }

    public void callOnDestroy() throws IOException, InterruptedException {
        plugin.onDestroy(this);
    }

    public Set<String> getSupportedAnnotations() {
        return plugin.getSupportedAnnotations();
    }

    @Nullable
    public ThirdRound.TransformProvider newTransformProvider() {
        ClassTransformer transformer = new ClassTransformWrapper(plugin.onTransformClass());
        return new ThirdRound.TransformProvider() {

            @Override
            public void onClassAffected(String className) {
                affected.add(className);
            }

            @Override
            public ClassTransformer transformer() {
                return transformer;
            }
        };
    }

    @Nullable
    public AnnotationClassDispatcher.AnnotationProcessorProvider newAnnotationProvider() {
        AnnotationProcessor processor = plugin.onProcessAnnotations();
        if (processor != null) {
            return new AnnotationClassDispatcher.AnnotationProcessorProvider() {
                Set<String> supported = plugin.getSupportedAnnotations();

                @Override
                public Set<String> supports() {
                    return supported;
                }

                @Override
                public AnnotationProcessor processor() {
                    return processor;
                }
            };
        }
        return null;
    }

    @Override
    public boolean isIncremental() {
        return incremental;
    }

    @Override
    public Arguments getArgs() {
        return args;
    }

    @Override
    public OutputProvider getOutputs() {
        return resource.provider(id);
    }

    public PluginBean toBean() {
        return new PluginBean(id, Sets.union(affected, preAffected));
    }

    public void combinePre(PluginBean pre) {
        preAffected = new ConcurrentHashSet<>(pre.getAffectedClasses());
    }

    private static final ClassTransformer NOOP = new ClassTransformer() {
        @Override
        public ClassRequest beforeTransform() {
            return new ClassRequest() {
            };
        }

        @Nullable
        @Override
        public CaptClassVisitor onTransform(ClassInfo classInfo, boolean required) {
            return null;
        }
    };

    class ClassTransformWrapper extends ClassTransformer {
        private final ClassTransformer classTransformer;

        ClassTransformWrapper(@Nullable ClassTransformer classTransformer) {
            this.classTransformer = classTransformer == null ? NOOP : classTransformer;
        }

        @Override
        public ClassRequest beforeTransform() {
            return classTransformer.beforeTransform();
        }

        @Nullable
        @Override
        public CaptClassVisitor onTransform(ClassInfo classInfo, boolean required) {
            if (!preAffected.isEmpty()) {
                preAffected.remove(classInfo.name());
            }
            return classTransformer.onTransform(classInfo, required);
        }

        @Override
        public void afterTransform() throws IOException, InterruptedException {
            classTransformer.afterTransform();
        }
    }
}
