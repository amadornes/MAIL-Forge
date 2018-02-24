package test;

import mail.core.loader.LoaderImpl;
import mail.core.serial.JSONSerializationHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModClassLoader;

import java.io.File;
import java.net.URL;

@Mod(modid = "mailforge", name = "MAILForge")
public class MAILForge implements LoaderImpl.ClasspathManager {

    public MAILForge() {
        try {
            setup();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setup() throws Exception {
        JSONSerializationHandler.instance = new JSONSerializationHandlerImpl();
        LoaderImpl.INSTANCE.load(this);
    }

    @Override
    public ModClassLoader getClassLoader() {
        return Loader.instance().getModClassLoader();
    }

    @Override
    public File[] getClasspathSources() {
        return getClassLoader().getParentSources();
    }

    @Override
    public void addSources(URL url) {
        try {
            getClassLoader().addFile(new File(url.toURI()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}