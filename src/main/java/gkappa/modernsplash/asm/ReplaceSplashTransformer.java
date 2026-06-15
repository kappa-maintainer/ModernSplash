package gkappa.modernsplash.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;

import cpw.mods.fml.common.FMLLog;

public class ReplaceSplashTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) {
            return null;
        }
        if (name.startsWith("cpw.mods.fml.client.SplashProgress")) {
            try {
                String actual = "gkappa.modernsplash" + transformedName.substring(19);
                FMLLog.getLogger()
                    .info(actual);
                ClassReader reader = new ClassReader(Launch.classLoader.getClassBytes(actual));
                ClassWriter writer = new ClassWriter(0);
                ClassVisitor visitor = new RemappingClassAdapter(writer, new SplashRemapper());
                reader.accept(visitor, ClassReader.EXPAND_FRAMES);
                return writer.toByteArray();
            } catch (Exception e) {
                FMLLog.getLogger()
                    .warn("Couldn't remap class {}", transformedName, e);
                return basicClass;
            }

        } else {
            return basicClass;
        }
    }

    static class SplashRemapper extends Remapper {

        @Override
        public String map(String typeName) {
            if (typeName.startsWith("gkappa/modernsplash/SplashProgress")) {
                return "cpw/mods/fml/client/" + typeName.substring(20);
            } else {
                return typeName;
            }
        }
    }
}
