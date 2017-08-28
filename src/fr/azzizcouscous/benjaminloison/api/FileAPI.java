package fr.azzizcouscous.benjaminloison.api;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import fr.azzizcouscous.benjaminloison.main.Main;

public class FileAPI
{
    public final static String path = new File("").getAbsolutePath() + File.separatorChar, pluginFolder = path + "plugins" + File.separatorChar, azzizFolder = pluginFolder + Main.NAME + File.separatorChar, skyWarsFolder = azzizFolder + "skyWars" + File.separatorChar, schematicsFolder = FileAPI.pluginFolder + "WorldEdit" + File.separatorChar + "Schematics" + File.separatorChar;
    public final static File languageFile = new File(FileAPI.azzizFolder + "language.txt");

    public static void initialize()
    {
        new File(FileAPI.azzizFolder).mkdirs();
        new File(FileAPI.skyWarsFolder).mkdirs();
        new File(FileAPI.schematicsFolder).mkdirs();
    }
    
    public static void write(String path, String toWrite)
    {
        try
        {
            FileWriter fileWriter = new FileWriter(path);
            fileWriter.write(toWrite);
            fileWriter.close();  
        }
        catch(IOException e)
        {
            Main.warn("Error while writing: " + toWrite + " in " + path);
            e.printStackTrace();
        }     
    }
}