package me.ahoo.cosid;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * IdGenerator decorator.
 *
 * @author ahoo wang
 */
@ThreadSafe
public interface IdGeneratorDecorator extends IdGenerator {
    /**
     * Get decorator actual id generator.
     *
     * @return actual id generator
     */
    @Nonnull
    IdGenerator getActual();
    
    @SuppressWarnings("unchecked")
    static <T extends IdGenerator> T getActual(IdGenerator idGenerator) {
        if (idGenerator instanceof IdGeneratorDecorator) {
            return getActual(((IdGeneratorDecorator) idGenerator).getActual());
        }
        return (T) idGenerator;
    }
    
    @Override
    default long generate() {
        return getActual().generate();
    }
    
}
