package com.vea.clasp.plugin.api.process;

import com.vea.clasp.plugin.api.hint.Type;
import com.vea.clasp.plugin.api.hint.Thread;

import org.objectweb.asm.tree.ClassNode;

public interface ClassConsumer {
    @Thread(Type.COMPUTATION)
    void accept(ClassNode node);
}
