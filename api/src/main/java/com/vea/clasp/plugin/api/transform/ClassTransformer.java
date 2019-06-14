package com.vea.clasp.plugin.api.transform;

import com.vea.clasp.plugin.api.asm.CaptClassVisitor;
import com.vea.clasp.plugin.api.graph.ClassInfo;
import com.vea.clasp.plugin.api.hint.Type;
import com.vea.clasp.plugin.api.hint.Thread;
import java.io.IOException;

import javax.annotation.Nullable;

public abstract class ClassTransformer {
    /**
     * After parse every meta class, tell capt which classes require to transform.
     *
     * @return class request
     */
    @Thread(Type.COMPUTATION)
    public ClassRequest beforeTransform() {
        return new ClassRequest();
    }

    /**
     * @param classInfo the basic info of class
     * @param required  true if the class  in your ClassRequest
     * @return the class visitor to participate in class transform.
     */
    @Thread(Type.COMPUTATION)
    @Nullable
    public abstract CaptClassVisitor onTransform(ClassInfo classInfo, boolean required);

    /**
     * Invoked after all class transform done.
     * @throws IOException io
     * @throws InterruptedException inter
     */
    @Thread(Type.IO)
    public void afterTransform() throws IOException, InterruptedException {
    }
}
