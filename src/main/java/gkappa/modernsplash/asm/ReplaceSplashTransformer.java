package gkappa.modernsplash.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.FMLLog;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

public class ReplaceSplashTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) {
            return null;
        }
        if (name.startsWith("net.minecraftforge.fml.client.SplashProgress")) {
            try {
                String actual = "gkappa.modernsplash.CustomSplash" + transformedName.substring(44);
                //FMLLog.log.info(actual);
                ClassReader reader = new ClassReader(Launch.classLoader.getClassBytes(actual));
                ClassWriter writer = new ClassWriter(0);
                ClassVisitor visitor = new ClassRemapper(writer, new SplashRemapper());
                reader.accept(visitor, ClassReader.EXPAND_FRAMES);
                return writer.toByteArray();
            } catch (Exception e) {
                FMLLog.log.warn("Couldn't remap class {}", transformedName, e);
                return basicClass;
            }
        } else {
            return basicClass;
        }
    }

    static class SplashRemapper extends Remapper {
        
        public SplashRemapper() {
            super(Opcodes.ASM9);
        }

        @Override
        public String map(String typeName) {
            if (typeName.startsWith("gkappa/modernsplash/CustomSplash")) {
                return "net/minecraftforge/fml/client/SplashProgress" + typeName.substring("gkappa/modernsplash/CustomSplash".length());
            } else {
                return typeName;
            }
        }
    }
}
