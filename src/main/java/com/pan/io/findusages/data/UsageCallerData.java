package com.pan.io.findusages.data;

import com.intellij.find.findUsages.PsiElement2UsageTargetAdapter;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiCall;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.*;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.handler.mxGraphTransferHandler;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import com.pan.io.findusages.action.FilterFindUsageManager;
import com.pan.io.findusages.config.FilterConfig;
import com.pan.io.findusages.service.SettingsFilterService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class UsageCallerData {

    private final Project project;
    private final PsiElement element;
    private UsageCallerNode callerNode;
    private final List<UsageCall> allUsageCall;
    private final List<PsiCall> allPsiCall;


    public UsageCallerData(Project project, PsiElement element) {
        this.project = project;
        this.element = element;
        this.allUsageCall = new ArrayList<>();
        this.allPsiCall = new ArrayList<>();
    }


    public void addCall(UsageCall usage) {
        allUsageCall.add(usage);
    }

    public void addCall(PsiCall call) {
        allPsiCall.add(call);
    }

    public boolean isContainCall(PsiCall call) {
        return allPsiCall.contains(call);
    }

    public Project getProject() {
        return project;
    }

    public PsiElement getElement() {
        return element;
    }

    public void setCallerNode(UsageCallerNode callerNode) {
        this.callerNode = callerNode;
    }

    public List<UsageCall> getAllCallerNodes() {
        return allUsageCall;
    }

    public List<UsageCall> callerAllNodesWithFilter() {
        final FilterConfig filterConfig = SettingsFilterService.getSettingsStorage(project);
        if (StringUtils.isNotBlank(filterConfig.getFilterAnnotation())) {
            if (filterConfig.isOnlyCaller()) {
                return allUsageCall.stream()
                        .filter(e -> FilterFindUsageManager.isAvailable(e.getMyAnnotation(), e.getAnnotationList(), filterConfig))
                        .toList();
            }
            List<UsageCall> calls = new ArrayList<>();
            List<UsageCallerNode> callerNodeList = callerNode.getNextNodes();

            for (UsageCallerNode callerNode : callerNodeList) {
                List<UsageCall> callTree = new ArrayList<>();
                loopFindAddCall(calls, callerNode, callTree, filterConfig, false);
                // 这里可以打印出调用链路
            }
            UsageCall call = callerNode.getCall();
            // 添加自身
            if (!calls.contains(call) && FilterFindUsageManager.isAvailable(call.getMyAnnotation(), call.getAnnotationList(), filterConfig)) {
                calls.add(call);
            }
            return calls;
        }
        return allUsageCall;
    }


    public static boolean loopFindAddCall(List<UsageCall> allUsages, UsageCallerNode callerNode, List<UsageCall> callTree, FilterConfig filterConfig, boolean isFilter) {
        // 添加调用者
        callTree.add(callerNode.getCall());

        isFilter = isFilter || FilterFindUsageManager.isAvailable(callerNode.getCall().getMyAnnotation(), callerNode.getCall().getAnnotationList(), filterConfig);

        if (CollectionUtils.isEmpty(callerNode.getNextNodes())) {
            if (isFilter) {
                callTree.stream()
                        .filter(e -> !allUsages.contains(e))
                        .forEach(allUsages::add);
            }
            // 调用树终止
            return isFilter;
        }
        for (UsageCallerNode nextCallerNode : callerNode.getNextNodes()) {

            isFilter = isFilter || loopFindAddCall(allUsages, nextCallerNode, callTree, filterConfig, isFilter);
        }
        return isFilter;
    }

    public void showCallTree() {
        final FilterConfig filterConfig = SettingsFilterService.getSettingsStorage(project);
        List<UsageCall> callerList = callerAllNodesWithFilter();

        UsageViewManager usageViewManager = UsageViewManager.getInstance(project);

        UsageViewPresentation usageViewPresentation = new UsageViewPresentation();
        usageViewPresentation.setTargetsNodeText("Filter Find Usage");
        usageViewPresentation.setCodeUsagesString("Find Result Filter With [" + filterConfig.getFilterAnnotation() + " ]");
        usageViewPresentation.setTabText("Filter Find Usage");
        // 对象转换
        UsageTarget[] usageTargets = {new PsiElement2UsageTargetAdapter(element, true)};

        int count = callerList.size();
        PsiElement[] primaryElements = new PsiElement[count];
        UsageInfo[] usageInfo = new UsageInfo[count];
        // 组装界面数据
        for (int i = 0; i < callerList.size(); i++) {
            UsageCall call = callerList.get(i);
            // 获取调用者
            primaryElements[i] = call.getCaller();
            usageInfo[i] = new UsageInfo(call.getCaller());

        }
        // 显示
        if (usageViewManager != null) {
            // close
            UsageView usageView = usageViewManager.getSelectedUsageView();
            if (usageView != null) {
                usageView.close();
            }
        }
        if (primaryElements.length == 0) {
            UsageViewManager.getInstance(project).showUsages(UsageTarget.EMPTY_ARRAY, new Usage[]{}, usageViewPresentation);
        } else {
            Usage[] usages = UsageInfoToUsageConverter.convert(primaryElements, usageInfo);
            assert usageViewManager != null;
            usageViewManager.showUsages(usageTargets, usages, usageViewPresentation);
        }

        drawGraph();
    }

    static final int width = 80;
    static final int height = 30;
    static final int xMove = 200;
    static final int yMove = 60;

    public boolean drawNode(final FilterConfig filterConfig, List<MxCell> allCells, mxGraph graph, Object rootNode, mxCell parent, UsageCallerNode node, final int x, final int y) {
        // filter
        boolean available = FilterFindUsageManager.isAvailable(node.getCall().getMyAnnotation(), node.getCall().getAnnotationList(), filterConfig);
        int xm = available ? x + xMove : x;

        PsiElement caller = node.getCall().getCaller();
        PsiElement callMethod = node.getCall().getCallMethod();

        MxCell mxCell = new MxCell(callMethod);

        // 连线
        MxCell lineCell = new MxCell(caller);
        mxCell nodeCell = (mxCell) graph.createVertex(rootNode, callMethod.getText(), mxCell, xm, y, width, height, null, false);
        if (available) {
            graph.addCell(nodeCell);

            if (allCells.contains(mxCell)) {
                nodeCell.setStyle("fillColor=yellow;");
            }
            graph.insertEdge(rootNode, caller.getText(), lineCell, parent, nodeCell, "edgeStyle=elbowEdgeStyle");
        }
        allCells.add(lineCell);
        allCells.add(mxCell);

        // 跳过中间调用节点
        if (!available) {
            nodeCell = parent;
        }
        List<UsageCallerNode> nodes = node.getNextNodes();
        if (CollectionUtils.isEmpty(nodes)) {
            return available;
        }
        int ym = y;
        for (UsageCallerNode nextNode : nodes) {
            boolean drawn = drawNode(filterConfig, allCells, graph, rootNode, nodeCell, nextNode, xm, ym);
            if (drawn) {
                ym += yMove;
            }
        }
        return available;
    }

    private static final JFrame frame = new JFrame("Call Tree");

    static {
        // 创建调用链路图
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setMinimumSize(new Dimension(400, 300));
        // 设置 preferredSize
        frame.setPreferredSize(new Dimension(800, 600));
        // 居中
        frame.setLocationRelativeTo(null);
        // 显示
        frame.setVisible(false);
    }

    public void drawGraph() {
        final FilterConfig filterConfig = SettingsFilterService.getSettingsStorage(project);
        boolean showCallTree = filterConfig.isShowCallTree();
        if (!showCallTree || callerNode == null || CollectionUtils.isEmpty(callerNode.getNextNodes())) {
            clearFrameContent(frame);
            frame.setVisible(false);
            return;
        }

        mxGraph graph = new mxGraph();
        Object rootNode = graph.getDefaultParent();

        graph.getModel().beginUpdate();
        try {
            int x = 20;
            int y = 20;
            List<MxCell> allCells = new ArrayList<>();

            List<UsageCallerNode> nodes = callerNode.getNextNodes();
            MxCell mxCell = new MxCell(element);
            allCells.add(mxCell);


            mxCell start = (mxCell) graph.insertVertex(rootNode, element.getText(), mxCell, x, y, width, height);
            for (UsageCallerNode node : nodes) {
                drawNode(filterConfig, allCells, graph, rootNode, start, node, x, y);
                y += yMove;
            }
        } finally {
            graph.getModel().endUpdate();
        }

        // 禁止拖动边
        graph.setAllowDanglingEdges(false);
        graph.setCellsEditable(false);
        graph.setCellsMovable(true);
        graph.setCellsResizable(true);
        graph.setCellsDisconnectable(false);
        graph.setCellsCloneable(false);
        graph.setKeepEdgesInBackground(true);
        graph.setConnectableEdges(false);
        graph.setCellsMovable(true);


        mxGraphComponent graphComponent = new mxGraphComponent(graph);
        graphComponent.setAutoScroll(true);
        graphComponent.setConnectable(false);
        graphComponent.setAutoScroll(true);
        // 在组件上设置拖动操作
        graphComponent.getGraphControl().setTransferHandler(new mxGraphTransferHandler());
        // 添加双击事件
        graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2) {
                    return;
                }
                mxCell cell = (mxCell) graph.getSelectionCell();
                if (cell == null) {
                    return;
                }
                if (cell.getValue() instanceof MxCell mxCell) {
                    PsiElement element = mxCell.getElement();
                    if (element != null) {
                        navigateToElement(element);
                    }
                }
            }
        });
        clearFrameContent(frame);
        frame.add(graphComponent);
        // 自动调整大小
        frame.pack();
        frame.setVisible(true);
    }

    private static void clearFrameContent(JFrame frame) {
        Component[] components = frame.getContentPane().getComponents();
        for (Component component : components) {
            frame.remove(component);
        }
        frame.revalidate();
        frame.repaint();
    }

    private static void navigateToElement(PsiElement element) {
        PsiFile containingFile = element.getContainingFile();
        OpenFileDescriptor descriptor = new OpenFileDescriptor(element.getProject(), containingFile.getVirtualFile(), element.getTextOffset());
        descriptor.navigate(true);
    }

}
