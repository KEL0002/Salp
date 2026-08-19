package de.kel0002.salp.util;

import de.kel0002.salp.dialogs.main.FileListDialog;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.codehaus.plexus.util.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;

public class FileUtil {
    public static File getFile(String path) {
        return new File(Bukkit.getServer().getWorldContainer(), path);}

    public static String getPath(File file) {
        return cleanPath(Bukkit.getServer().getWorldContainer().toPath().relativize(file.toPath()) + "");}

    public static String getParentPath(String path) {return getPath(getFile(path).getParentFile());}

    public static String getNameOnly(String path) {return getFile(path).getName();}

    public static String cleanPath(String path){
        if (!path.startsWith("/")) path = "/" + path;
        if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
        if (getFile(path).isDirectory()) path = path + "/";
        return path;
    }

    public static void open(String path, Player player) {
        String cleanPath = cleanPath(path);
        File file = getFile(cleanPath);
        if (file.isDirectory()) new FileListDialog(cleanPath, player);
        else  {new FileListDialog(getParentPath(cleanPath), player).openTxtDialog(path);}
    }


    @NotNull
    public static File[] getFolderContents(String path) {
        File folder = getFile(path);
        if (!folder.exists() || !folder.isDirectory()) return new File[0];
        if (folder.listFiles() == null) return new File[0];
        return folder.listFiles();
    }

    public static FileTime getModificationTime(File file) {
        try {
            return Files.getLastModifiedTime(file.toPath());
        } catch (IOException e) {
            return FileTime.fromMillis(-1);
        }
    }

    public static FileTime getCreationTime(File file) {
        try {
            return (FileTime) Files.getAttribute(file.toPath(), "creationTime");
        } catch (IOException e) {
            return FileTime.fromMillis(-1);
        }
    }

    public static long getSize(File file) {
        if (file.isDirectory()) return FileUtils.sizeOfDirectory(file);
        else {
            try {
                return Files.size(file.toPath());
            } catch (IOException e) {
                return -1;
            }
        }
    }

    public static boolean isUsedDir(String path) {
        if (!Files.isDirectory(getFile(path).toPath())) return false;
        return !Arrays.stream(getFolderContents(path)).toList().isEmpty();
    }



    // DO STUFF

    public static MethodResult rename(String path, String newname) {
        boolean success = getFile(path).renameTo(getFile(getParentPath(path) + newname));
        return success ? new MethodResult() : new MethodResult("Error renaming File");
    }

    public static MethodResult delete(String path) {
        try {
            if (!isUsedDir(path)) Files.delete(getFile(path).toPath());
            else {FileUtils.deleteDirectory(getFile(path));}
            return new MethodResult();
        } catch (IOException e) {
            return new MethodResult(e.getMessage());
        }
    }

    public static MethodResult save(String path, String text) {
        try {
            Files.writeString(getFile(path).toPath(), text);
            return new MethodResult();
        } catch (IOException e) {
            return new MethodResult(e.getMessage());
        }
    }

    public static MethodResult create(String path, boolean isdir) {
        try {
            if (isdir) Files.createDirectory(getFile(path).toPath());
            else Files.createFile(getFile(path).toPath());
            return new MethodResult();
        } catch (IOException e) {
            return new MethodResult(e.getMessage());
        }
    }
}
