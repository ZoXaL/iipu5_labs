package com.zoxal.labs.iapd.usb.manager;

import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

public abstract class DirectoryFinder extends SimpleFileVisitor<Path> {
    protected List<Path> matchedPaths = new ArrayList<>();
    protected Map<Integer, Pattern> pathElementMatcherMap;

    protected boolean match(Path file) {
        int nameCount = file.getNameCount();
//        System.out.println(file + " - " + (nameCount - 1) + " < " + getMinPathLength());
        if (nameCount < getMinPathLength()) {
            return true;
        }
        Pattern elementPattern = pathElementMatcherMap.get(nameCount - 1);
        Path name = file.getName(nameCount - 1);

        boolean matches = elementPattern.matcher(name.toString()).find();
        if (nameCount >= getMaxPathLength()) {
            if (matches) {
                matchedPaths.add(file);
            }
            return false;
        }
        return matches;
    }

    protected abstract int getMinPathLength();
    protected abstract int getMaxPathLength();

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        return (match(dir)) ? CONTINUE : SKIP_SUBTREE;
    }

    public Collection<Path> getMatchedPaths() {
        return matchedPaths;
    }
}
