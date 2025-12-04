package com.xiaofan.macro;

import com.xiaofan.api.ILogger;
import com.xiaofan.api.VersionProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 宏文件解析器
 * 解析宏文件并构建宏对象
 * 
 * 此代码已迁移到 allcommon，使用抽象接口，可在 1.20.1-1.20.6 版本间复用
 */
public class MacroParser {
    private static ILogger getLogger() {
        return VersionProvider.getVersion().getLogger();
    }
    
    /**
     * 解析宏文件
     */
    public static Macro parse(File file) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath());
        Macro macro = new Macro();
        macro.name = file.getName().replace(".txt", "");
        
        // 移除注释和空行
        List<String> cleanLines = new ArrayList<>();
        for (String line : lines) {
            String trimmed = line.trim();
            // 移除行尾注释
            int commentIndex = trimmed.indexOf("//");
            if (commentIndex >= 0) {
                trimmed = trimmed.substring(0, commentIndex).trim();
            }
            if (!trimmed.isEmpty()) {
                cleanLines.add(trimmed);
            }
        }
        
        // 查找主函数入口 fan_main:
        int mainIndex = -1;
        for (int i = 0; i < cleanLines.size(); i++) {
            if (cleanLines.get(i).equals("fan_main:") || cleanLines.get(i).startsWith("fan_main:")) {
                mainIndex = i;
                break;
            }
        }
        
        if (mainIndex >= 0) {
            // 找到主函数入口
            // 第一步：先解析 fan_main: 之前的所有函数定义（如果有）
            if (mainIndex > 0) {
                parseMacroContent(cleanLines, macro, 0, mainIndex);
            }
            // 第二步：解析 fan_main: 之后的内容
            // 先解析所有函数定义，再解析主函数命令
            int mainStart = mainIndex + 1;
            
            // 先扫描并解析所有函数定义
            int i = mainStart;
            while (i < cleanLines.size()) {
                String line = cleanLines.get(i).trim();
                if (line.startsWith("fun ")) {
                    Function func = parseFunction(cleanLines, i, cleanLines.size());
                    if (func != null) {
                        macro.functions.put(func.name, func);
                        getLogger().info("[宏解析] 解析到函数: {} (后台执行: {})", func.name, func.isBackground);
                        i = func.endIndex;
                        continue;
                    }
                }
                i++;
            }
            
            // 然后解析主函数命令（跳过函数定义）
            Macro mainMacro = new Macro();
            i = mainStart;
            while (i < cleanLines.size()) {
                String line = cleanLines.get(i).trim();
                
                // 跳过函数定义（已经在上面解析过了）
                if (line.startsWith("fun ")) {
                    Function func = parseFunction(cleanLines, i, cleanLines.size());
                    if (func != null) {
                        i = func.endIndex;
                        continue;
                    }
                }
                
                // 解析主函数命令
                if (line.startsWith("if ")) {
                    IfStatement ifStmt = parseIfStatement(line);
                    i++;
                    
                    List<MacroCommand> ifCommands = new ArrayList<>();
                    List<MacroCommand> elseCommands = new ArrayList<>();
                    boolean inElse = false;
                    int depth = 1;
                    
                    while (i < cleanLines.size() && depth > 0) {
                        String currentLine = cleanLines.get(i).trim();
                        
                        // 跳过函数定义
                        if (currentLine.startsWith("fun ")) {
                            Function func = parseFunction(cleanLines, i, cleanLines.size());
                            if (func != null) {
                                i = func.endIndex;
                                continue;
                            }
                        }
                        
                        if (currentLine.startsWith("if ")) {
                            depth++;
                            int nestedEnd = findNestedEnd(cleanLines, i, cleanLines.size());
                            Macro nestedMacro = new Macro();
                            int nextIndex = parseMacroContent(cleanLines, nestedMacro, i, nestedEnd);
                            if (inElse) {
                                elseCommands.addAll(nestedMacro.commands);
                            } else {
                                ifCommands.addAll(nestedMacro.commands);
                            }
                            i = nextIndex;
                            continue;
                        } else if (currentLine.equals("else") || currentLine.equals("else;")) {
                            if (depth == 1) {
                                inElse = true;
                                i++;
                                continue;
                            }
                        } else if (currentLine.equals("end;") || currentLine.equals("end")) {
                            depth--;
                            if (depth == 0) {
                                i++;
                                break;
                            }
                        }
                        
                        if (depth > 0) {
                            if (inElse && depth == 1) {
                                MacroCommand cmd = parseCommand(currentLine);
                                if (cmd != null) {
                                    elseCommands.add(cmd);
                                }
                            } else if (!inElse && depth == 1) {
                                MacroCommand cmd = parseCommand(currentLine);
                                if (cmd != null) {
                                    ifCommands.add(cmd);
                                }
                            }
                        }
                        i++;
                    }
                    
                    ifStmt.ifCommands = ifCommands;
                    ifStmt.elseCommands = elseCommands;
                    mainMacro.commands.add(ifStmt);
                    continue;
                }
                
                MacroCommand cmd = parseCommand(line);
                if (cmd != null) {
                    mainMacro.commands.add(cmd);
                }
                
                i++;
            }
            
            // 将主函数命令存储到 macro.mainCommands（如果存在）或 macro.commands
            macro.commands.addAll(mainMacro.commands);
        } else {
            // 没有主函数入口，按原来的方式解析
            parseMacroContent(cleanLines, macro, 0, cleanLines.size());
        }
        
        getLogger().info("[宏解析] 解析完成，共 {} 个命令，{} 个函数", macro.commands.size(), macro.functions.size());
        if (!macro.functions.isEmpty()) {
            getLogger().info("[宏解析] 函数列表: {}", macro.functions.keySet());
        }
        if (!macro.commands.isEmpty()) {
            getLogger().info("[宏解析] 命令类型: {}", macro.commands.stream()
                .map(cmd -> cmd.getClass().getSimpleName())
                .toList());
        }
        
        return macro;
    }
    
    /**
     * 解析宏内容（递归解析 if-else 和函数）
     */
    private static int parseMacroContent(List<String> lines, Macro macro, int startIndex, int endIndex) {
        int i = startIndex;
        while (i < endIndex && i < lines.size()) {
            String line = lines.get(i);
            
            // 解析 if 语句
            if (line.startsWith("if ")) {
                IfStatement ifStmt = parseIfStatement(line);
                i++; // 跳过 if 行
                
                // 解析 if 块
                List<MacroCommand> ifCommands = new ArrayList<>();
                List<MacroCommand> elseCommands = new ArrayList<>();
                boolean inElse = false;
                int depth = 1;
                
                while (i < endIndex && i < lines.size() && depth > 0) {
                    String currentLine = lines.get(i).trim();
                    
                    if (currentLine.startsWith("if ")) {
                        depth++;
                        // 嵌套的 if，递归解析
                        int nestedEnd = findNestedEnd(lines, i, endIndex);
                        Macro nestedMacro = new Macro();
                        int nextIndex = parseMacroContent(lines, nestedMacro, i, nestedEnd);
                        // 将嵌套的宏命令添加到当前块
                        if (inElse) {
                            elseCommands.addAll(nestedMacro.commands);
                        } else {
                            ifCommands.addAll(nestedMacro.commands);
                        }
                        i = nextIndex;
                        continue;
                    } else if (currentLine.equals("else") || currentLine.equals("else;")) {
                        if (depth == 1) {
                            inElse = true;
                            i++;
                            continue;
                        }
                    } else if (currentLine.equals("end;") || currentLine.equals("end")) {
                        depth--;
                        if (depth == 0) {
                            i++;
                            break;
                        }
                    }
                    
                    if (depth > 0) {
                        if (inElse && depth == 1) {
                            // else 块中的命令
                            MacroCommand cmd = parseCommand(currentLine);
                            if (cmd != null) {
                                elseCommands.add(cmd);
                            }
                        } else if (!inElse && depth == 1) {
                            // if 块中的命令
                            MacroCommand cmd = parseCommand(currentLine);
                            if (cmd != null) {
                                ifCommands.add(cmd);
                            }
                        }
                    }
                    i++;
                }
                
                ifStmt.ifCommands = ifCommands;
                ifStmt.elseCommands = elseCommands;
                macro.commands.add(ifStmt);
                continue;
            }
            
            // 解析函数定义
            if (line.startsWith("fun ")) {
                Function func = parseFunction(lines, i, endIndex);
                if (func != null) {
                    macro.functions.put(func.name, func);
                    i = func.endIndex;
                    continue;
                }
            }
            
            // 解析普通命令
            MacroCommand cmd = parseCommand(line);
            if (cmd != null) {
                macro.commands.add(cmd);
            }
            
            i++;
        }
        
        return i;
    }
    
    /**
     * 解析 if 语句
     */
    private static IfStatement parseIfStatement(String line) {
        IfStatement stmt = new IfStatement();
        
        // 解析 if me at = (x,y,z)
        Pattern pattern = Pattern.compile("if\\s+me\\s+at\\s*=\\s*\\(\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*\\)");
        Matcher matcher = pattern.matcher(line);
        if (matcher.matches()) {
            stmt.type = IfStatement.Type.POSITION;
            stmt.x = Integer.parseInt(matcher.group(1));
            stmt.y = Integer.parseInt(matcher.group(2));
            stmt.z = Integer.parseInt(matcher.group(3));
            return stmt;
        }
        
        // 解析 if time = 11000
        pattern = Pattern.compile("if\\s+time\\s*=\\s*(\\d+)");
        matcher = pattern.matcher(line);
        if (matcher.matches()) {
            stmt.type = IfStatement.Type.TIME;
            stmt.time = Long.parseLong(matcher.group(1));
            stmt.timeComparison = IfStatement.TimeComparison.EQUAL;
            return stmt;
        }
        
        // 解析 if time >= 11000
        pattern = Pattern.compile("if\\s+time\\s*>=\\s*(\\d+)");
        matcher = pattern.matcher(line);
        if (matcher.matches()) {
            stmt.type = IfStatement.Type.TIME;
            stmt.time = Long.parseLong(matcher.group(1));
            stmt.timeComparison = IfStatement.TimeComparison.GREATER_EQUAL;
            return stmt;
        }
        
        // 解析 if time <= 11000
        pattern = Pattern.compile("if\\s+time\\s*<=\\s*(\\d+)");
        matcher = pattern.matcher(line);
        if (matcher.matches()) {
            stmt.type = IfStatement.Type.TIME;
            stmt.time = Long.parseLong(matcher.group(1));
            stmt.timeComparison = IfStatement.TimeComparison.LESS_EQUAL;
            return stmt;
        }
        
        getLogger().warn("[宏解析] 无法解析 if 语句: {}", line);
        return stmt;
    }
    
    /**
     * 解析函数定义
     */
    private static Function parseFunction(List<String> lines, int startIndex, int endIndex) {
        String funcLine = lines.get(startIndex).trim();
        // fun name="name" type= &;
        Pattern pattern = Pattern.compile("fun\\s+name\\s*=\\s*\"([^\"]+)\"\\s*(?:type\\s*=\\s*&)?\\s*;?");
        Matcher matcher = pattern.matcher(funcLine);
        if (!matcher.matches()) {
            return null;
        }
        
        Function func = new Function();
        func.name = matcher.group(1);
        func.isBackground = funcLine.contains("type=") && funcLine.contains("&");
        func.startIndex = startIndex + 1;
        
        // 找到函数结束位置（下一个 fun 或文件结束）
        int i = startIndex + 1;
        int depth = 0;
        while (i < endIndex && i < lines.size()) {
            String currentLine = lines.get(i).trim();
            if (currentLine.startsWith("fun ")) {
                if (depth == 0) {
                    break;
                }
            } else if (currentLine.startsWith("if ")) {
                depth++;
            } else if (currentLine.equals("end;") || currentLine.equals("end")) {
                depth--;
            }
            i++;
        }
        func.endIndex = i;
        
        // 解析函数内容
        Macro funcMacro = new Macro();
        parseMacroContent(lines, funcMacro, func.startIndex, func.endIndex);
        func.commands = funcMacro.commands;
        
        return func;
    }
    
    /**
     * 解析命令
     */
    private static MacroCommand parseCommand(String line) {
        if (line.isEmpty() || line.equals("end;") || line.equals("end")) {
            return null;
        }
        
        // check 命令
        if (line.startsWith("check ")) {
            String content = line.substring(6).trim();
            if (content.endsWith(";")) {
                content = content.substring(0, content.length() - 1).trim();
            }
            
            CheckCommand cmd = parseCheckCommand(content);
            if (cmd != null) {
                return cmd;
            }
        }
        
        // wait 命令
        if (line.startsWith("wait")) {
            String content = line.substring(4).trim();
            if (content.endsWith(";")) {
                content = content.substring(0, content.length() - 1).trim();
            }
            
            WaitCommand cmd = new WaitCommand();
            cmd.content = content; // 可能是空（一直等待）或 "xs"、"xm"、"xh" 格式
            return cmd;
        }
        
        // run 命令: run name = "回家"
        if (line.startsWith("run ")) {
            String content = line.substring(4).trim();
            if (content.endsWith(";")) {
                content = content.substring(0, content.length() - 1).trim();
            }
            
            // 解析 run name = "回家"
            Pattern runPattern = Pattern.compile("name\\s*=\\s*\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
            Matcher runMatcher = runPattern.matcher(content);
            if (runMatcher.find()) {
                String macroName = runMatcher.group(1);
                RunCommand cmd = new RunCommand();
                cmd.macroName = macroName;
                return cmd;
            } else {
                getLogger().warn("[宏解析] run 命令格式错误: {}", content);
                return null;
            }
        }
        
        // do #command args;
        if (line.startsWith("do ")) {
            String content = line.substring(3).trim();
            if (content.endsWith(";")) {
                content = content.substring(0, content.length() - 1).trim();
            }
            
            DoCommand cmd = new DoCommand();
            cmd.content = content;
            return cmd;
        }
        
        return null;
    }
    
    /**
     * 解析 check 命令
     * 语法1: check me have (item = Pickaxe,type = diamond,quantity = 1), do #goto 0 0 0;
     * 语法2: check me nothave (item = raw_iron), do #goto 0 0 0;
     * 语法3: check me at = (0,0,0),do end;
     * 语法4: check time = 11000,do /home;
     */
    private static CheckCommand parseCheckCommand(String content) {
        CheckCommand cmd = new CheckCommand();
        
        try {
            // 解析语法1: check me have (...), do ...
            Pattern havePattern = Pattern.compile("me\\s+have\\s*\\(([^)]+)\\)\\s*,\\s*do\\s+(.+)", Pattern.CASE_INSENSITIVE);
            Matcher haveMatcher = havePattern.matcher(content);
            if (haveMatcher.matches()) {
                cmd.type = CheckCommand.Type.ITEM;
                String params = haveMatcher.group(1);
                cmd.action = haveMatcher.group(2).trim();
                
                // 解析参数: item = Pickaxe,type = diamond,quantity = 1
                Pattern paramPattern = Pattern.compile("(\\w+)\\s*=\\s*([^,]+)");
                Matcher paramMatcher = paramPattern.matcher(params);
                while (paramMatcher.find()) {
                    String key = paramMatcher.group(1).trim().toLowerCase();
                    String value = paramMatcher.group(2).trim();
                    
                    if (key.equals("item")) {
                        cmd.itemName = value;
                    } else if (key.equals("type")) {
                        cmd.itemType = value;
                    } else if (key.equals("quantity")) {
                        try {
                            cmd.quantity = Integer.parseInt(value);
                            if (cmd.quantity < 1 || cmd.quantity > 64) {
                                getLogger().warn("[宏解析] check 命令 quantity 超出范围 (1-64): {}", cmd.quantity);
                                return null;
                            }
                        } catch (NumberFormatException e) {
                            getLogger().warn("[宏解析] check 命令 quantity 格式错误: {}", value);
                            return null;
                        }
                    }
                }
                
                if (cmd.itemName == null) {
                    getLogger().warn("[宏解析] check 命令缺少 item 参数");
                    return null;
                }
                if (cmd.quantity == null) {
                    cmd.quantity = 1; // 默认数量为1
                }
                
                return cmd;
            }
            
            // 解析语法2: check me nothave (item = raw_iron), do ...
            Pattern notHavePattern = Pattern.compile("me\\s+nothave\\s*\\(([^)]+)\\)\\s*,\\s*do\\s+(.+)", Pattern.CASE_INSENSITIVE);
            Matcher notHaveMatcher = notHavePattern.matcher(content);
            if (notHaveMatcher.matches()) {
                cmd.type = CheckCommand.Type.NOTHAVE;
                String params = notHaveMatcher.group(1);
                cmd.action = notHaveMatcher.group(2).trim();
                
                // 解析参数: item = raw_iron (nothave 只允许 item 参数)
                Pattern paramPattern = Pattern.compile("(\\w+)\\s*=\\s*([^,]+)");
                Matcher paramMatcher = paramPattern.matcher(params);
                while (paramMatcher.find()) {
                    String key = paramMatcher.group(1).trim().toLowerCase();
                    String value = paramMatcher.group(2).trim();
                    
                    if (key.equals("item")) {
                        cmd.itemName = value;
                    } else {
                        // nothave 只允许 item 参数，其他参数报错
                        getLogger().warn("[宏解析] check nothave 命令只允许 item 参数，不允许: {}", key);
                        return null;
                    }
                }
                
                if (cmd.itemName == null) {
                    getLogger().warn("[宏解析] check nothave 命令缺少 item 参数");
                    return null;
                }
                
                return cmd;
            }
            
            // 解析语法3: check me at = (x,y,z),do ...
            Pattern atPattern = Pattern.compile("me\\s+at\\s*=\\s*\\(\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*\\)\\s*,\\s*do\\s+(.+)", Pattern.CASE_INSENSITIVE);
            Matcher atMatcher = atPattern.matcher(content);
            if (atMatcher.matches()) {
                cmd.type = CheckCommand.Type.POSITION;
                cmd.x = Integer.parseInt(atMatcher.group(1));
                cmd.y = Integer.parseInt(atMatcher.group(2));
                cmd.z = Integer.parseInt(atMatcher.group(3));
                cmd.action = atMatcher.group(4).trim();
                return cmd;
            }
            
            // 解析语法4: check time = 11000,do ...
            Pattern timePattern = Pattern.compile("time\\s*=\\s*(\\d+)\\s*,\\s*do\\s+(.+)", Pattern.CASE_INSENSITIVE);
            Matcher timeMatcher = timePattern.matcher(content);
            if (timeMatcher.matches()) {
                cmd.type = CheckCommand.Type.TIME;
                cmd.time = Long.parseLong(timeMatcher.group(1));
                cmd.action = timeMatcher.group(2).trim();
                return cmd;
            }
            
            getLogger().warn("[宏解析] check 命令格式错误: {}", content);
            return null;
            
        } catch (Exception e) {
            getLogger().error("[宏解析] 解析 check 命令时出错: " + content, e);
            return null;
        }
    }
    
    /**
     * 查找嵌套的 end
     */
    private static int findNestedEnd(List<String> lines, int startIndex, int endIndex) {
        int depth = 1;
        int i = startIndex + 1;
        while (i < endIndex && i < lines.size() && depth > 0) {
            String line = lines.get(i);
            if (line.startsWith("if ")) {
                depth++;
            } else if (line.equals("end;") || line.equals("end")) {
                depth--;
            }
            if (depth > 0) {
                i++;
            }
        }
        return i;
    }
}

/**
 * 宏对象
 */
class Macro {
    String name;
    List<MacroCommand> commands = new ArrayList<>();
    Map<String, Function> functions = new HashMap<>();
}

/**
 * 函数对象
 */
class Function {
    String name;
    boolean isBackground;
    List<MacroCommand> commands = new ArrayList<>();
    Map<String, Function> functions = new HashMap<>(); // 函数可以访问宏的所有函数定义（用于递归调用）
    int startIndex;
    int endIndex;
}

/**
 * 命令接口
 */
interface MacroCommand {
    void execute(MacroExecutor executor);
}

/**
 * Do 命令
 */
class DoCommand implements MacroCommand {
    String content;
    
    @Override
    public void execute(MacroExecutor executor) {
        executor.executeDoCommand(content);
    }
}

/**
 * Wait 命令
 */
class WaitCommand implements MacroCommand {
    String content; // 空字符串表示一直等待，否则是 "xs"、"xm"、"xh" 格式
    
    @Override
    public void execute(MacroExecutor executor) {
        executor.executeWaitCommand(content);
    }
}

/**
 * Check 命令
 */
class CheckCommand implements MacroCommand {
    enum Type {
        ITEM,      // 物品检查 (have)
        NOTHAVE,   // 没有物品检查 (nothave)
        POSITION,  // 位置检查
        TIME       // 时间检查
    }
    
    Type type;
    String action; // 条件满足时执行的动作
    
    // 物品检查参数
    String itemName;  // 物品名称，如 "Pickaxe"
    String itemType;  // 物品类型，如 "diamond"（仅工具类物品）
    Integer quantity; // 数量 (1-64)
    
    // 位置检查参数
    int x, y, z;
    
    // 时间检查参数
    long time;
    
    @Override
    public void execute(MacroExecutor executor) {
        executor.executeCheckCommand(this);
    }
}

/**
 * Run 命令
 */
class RunCommand implements MacroCommand {
    String macroName; // 要启动的宏名称
    
    @Override
    public void execute(MacroExecutor executor) {
        executor.executeRunCommand(macroName);
    }
}

/**
 * If 语句
 */
class IfStatement implements MacroCommand {
    enum Type {
        POSITION,
        TIME
    }
    
    enum TimeComparison {
        EQUAL,
        GREATER_EQUAL,
        LESS_EQUAL
    }
    
    Type type;
    int x, y, z;
    long time;
    TimeComparison timeComparison = TimeComparison.EQUAL;
    List<MacroCommand> ifCommands = new ArrayList<>();
    List<MacroCommand> elseCommands = new ArrayList<>();
    
    @Override
    public void execute(MacroExecutor executor) {
        // IfStatement 的执行逻辑需要访问版本接口，将在 MacroExecutor 中实现
        executor.executeIfStatement(this);
    }
}

