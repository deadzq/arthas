package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.klass100.ClassLoaderCommand.ClassLoaderStat;
import com.taobao.arthas.core.command.model.ClassLoaderModel;
import com.taobao.arthas.core.command.model.ClassLoaderVO;
import com.taobao.arthas.core.command.model.ClassSetVO;
import com.taobao.arthas.core.command.model.ClassVO;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.util.ClassUtils;
import com.taobao.text.Decoration;
import com.taobao.text.ui.*;
import com.taobao.text.util.RenderUtil;

import java.util.List;
import java.util.Map;

/**
 * @author gongdewei 2020/4/21
 */
public class ClassLoaderView extends ResultView<ClassLoaderModel> {

    @Override
    public void draw(CommandProcess process, ClassLoaderModel result) {
        if (result.getAllClasses() != null) {
            drawAllClasses(process, result.getAllClasses());
        }
        if (result.getResources() != null) {
            drawResources(process, result.getResources());
        }
        if (result.getLoadClass() != null) {
            drawLoadClass(process, result.getLoadClass());
        }
        if (result.getUrls() != null) {
            drawClassLoaderUrls(process, result.getUrls());
        }
        if (result.getClassLoaders() != null){
            drawClassLoaders(process, result.getClassLoaders(), result.getTree());
        }
        if (result.getClassLoaderStats() != null){
            drawClassLoaderStats(process, result.getClassLoaderStats());
        }
    }

    private void drawClassLoaderStats(CommandProcess process, Map<String, ClassLoaderStat> classLoaderStats) {
        Element element = renderStat(classLoaderStats);
        process.write(RenderUtil.render(element, process.width()))
                .write(com.taobao.arthas.core.util.Constants.EMPTY_STRING);

    }

    private static TableElement renderStat(Map<String, ClassLoaderStat> classLoaderStats) {
        TableElement table = new TableElement().leftCellPadding(1).rightCellPadding(1);
        table.add(new RowElement().style(Decoration.bold.bold()).add("name", "numberOfInstances", "loadedCountTotal"));
        for (Map.Entry<String, ClassLoaderStat> entry : classLoaderStats.entrySet()) {
            table.row(entry.getKey(), "" + entry.getValue().getNumberOfInstance(), "" + entry.getValue().getLoadedCount());
        }
        return table;
    }

    private void drawClassLoaders(CommandProcess process, List<ClassLoaderVO> classLoaders, Boolean isTree) {
        Element element = isTree ? renderTree(classLoaders) : renderTable(classLoaders);
        process.write(RenderUtil.render(element, process.width()))
                .write(com.taobao.arthas.core.util.Constants.EMPTY_STRING);
    }

    private void drawClassLoaderUrls(CommandProcess process, List<String> urls) {
        process.write(RenderUtil.render(renderClassLoaderUrls(urls), process.width()));
        process.write(com.taobao.arthas.core.util.Constants.EMPTY_STRING);
    }

    private void drawLoadClass(CommandProcess process, ClassVO loadClass) {
        process.write(RenderUtil.render(ClassUtils.renderClassInfo(loadClass), process.width()) + "\n");
    }

    private void drawAllClasses(CommandProcess process, List<ClassSetVO> allClasses) {
        process.write(RenderUtil.render(renderClasses(allClasses), process.width()));
    }

    private void drawResources(CommandProcess process, List<String> resources) {
        TableElement table = new TableElement().leftCellPadding(1).rightCellPadding(1);
        for (String resource : resources) {
            table.row(resource);
        }
        process.write(RenderUtil.render(table, process.width()) + "\n");
        process.write(com.taobao.arthas.core.util.Constants.EMPTY_STRING);
    }

    private Element renderClasses(List<ClassSetVO> allClasses) {
        TableElement table = new TableElement().leftCellPadding(1).rightCellPadding(1);

        for (ClassSetVO classSetVO : allClasses) {
            table.row(new LabelElement("hash:" + classSetVO.getClassloader().getHash() + ", " + classSetVO.getClassloader().getName())
                    .style(Decoration.bold.bold()));
            for (String className : classSetVO.getClasses()) {
                table.row(new LabelElement(className));
            }
            table.row(new LabelElement(" "));
        }

        return table;
    }

    private static Element renderClassLoaderUrls(List<String> urls) {
        StringBuilder sb = new StringBuilder();
        for (String url : urls) {
            sb.append(url).append("\n");
        }
        return new LabelElement(sb.toString());
    }

    // 统计所有的ClassLoader的信息
    private static TableElement renderTable(List<ClassLoaderVO> classLoaderInfos) {
        TableElement table = new TableElement().leftCellPadding(1).rightCellPadding(1);
        table.add(new RowElement().style(Decoration.bold.bold()).add("name", "loadedCount", "hash", "parent"));
        for (ClassLoaderVO classLoaderVO : classLoaderInfos) {
            table.row(classLoaderVO.getName(), "" + classLoaderVO.getLoadedCount(), classLoaderVO.getHash(), classLoaderVO.getParent());
        }
        return table;
    }

    // 以树状列出ClassLoader的继承结构
    private static Element renderTree(List<ClassLoaderVO> classLoaderInfos) {
        TreeElement root = new TreeElement();
        for (ClassLoaderVO classLoader : classLoaderInfos) {
            TreeElement child = new TreeElement(classLoader.getName());
            root.addChild(child);
            renderSubtree(child, classLoader);
        }
        return root;
    }

    private static void renderSubtree(TreeElement parent, ClassLoaderVO parentClassLoader) {
        if (parentClassLoader.getChildren() == null){
            return;
        }
        for (ClassLoaderVO childClassLoader : parentClassLoader.getChildren()) {
            TreeElement child = new TreeElement(childClassLoader.getName());
            parent.addChild(child);
            renderSubtree(child, childClassLoader);
        }
    }
}
