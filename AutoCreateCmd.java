package org.example;

import com.squareup.protoparser.*;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hyy
 * @desc  用法 ：协议号的常量命名（ 跟协议的消息内容message （去掉 req resp后）一致 （头部大小写有区分而已）
 *        有些协议只是同步不需要生成cmd 则在协议号写 skip 关键字
 *        默认是module的生成 如果是生成在mgr模块的 则在协议后后面备注 mgr 关键字
 *        跨服如果没有返回区服的协议（直接返回给客户端那种）则协议号后面两位跟区服协议号保持一致，就可以自动映射到区服返回跟resp消息体
 *        跨服如果有返回 则协议号命名以 Ctg开头(区服上跨服则已Gtc开头） 然后后面命名跟区服的协议号一致 然后必须在跨服定义Gtc开头的resp
 *        协议号后面备注可以生成desc
 *
 * @date 2024/8/28 11:12
 */
public class AutoCreateCmd {

    public static List<String> messagList;

    public static List<String> messagList2 = new ArrayList<>();
    public static List<TypeElement> elList2 = new ArrayList<>();

    public static String pbPackPath2;
    public static String ROOT_PATH = "D:\\javaPro\\game\\";

    public static String fileName2;
    public static String fileName;

    public static String finalPackageModuleName;

    public static String author = "hyy";

    public static Map<Integer, String> elListMap2 = new HashMap<>();//协助的code 数值对应code string

    public static Map<Integer, String> elListCmdMap2 = new HashMap<>();//协助的code 数值对应code string
    public static String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    /**
     * 填入pb文件  注释带有mgr表示manger里面的模块 带有 skip 表示不生成 跨服respene -10000 对应code
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        String pbPath2 = "PBProtocol\\crossproto\\system\\CrossDestinyFight.proto";
        // String pbPath = "PBProtocol\\crossproto\\system\\CrossFairyLandTest.proto";
        String pbPath = "PBProtocol\\protofile\\destinyFight\\DestinyFight.proto";
        // String pbPath = "PBProtocol\\protofile\\player\\FairyLandTest.proto";
        // String pbPath2 = "crossproto/system/CrossDestinyFight.proto";//副本 用来辅助代码生成
        //String pbPath2 = "";
        execPb();
        if (args.length > 2) {
            ROOT_PATH = args[0];
            pbPath = args[1];
        }
        System.out.println(Arrays.toString(args));
        String pbFilePath = ROOT_PATH + pbPath;

        File pbFile = new File(pbFilePath);
        ProtoFile pb = ProtoParser.parseUtf8(pbFile);
        String filePath = pb.filePath();
        String fileNamePath = filePath.split("\\.")[0];
        String[] fileNameArr = fileNamePath.split("\\\\");
        fileName = fileNameArr[fileNameArr.length - 1];
        OptionElement packageData = pb.options().get(0);
        List<TypeElement> elList = pb.typeElements();
        if (!pbPath2.isEmpty()) {
            String pbFilePath2 = ROOT_PATH + pbPath2;
            File pbFile2 = new File(pbFilePath2);
            ProtoFile pb2 = ProtoParser.parseUtf8(pbFile2);
            elList2 = pb2.typeElements();
            String filePath2 = pb2.filePath();
            String fileNamePath2 = filePath2.split("\\.")[0];
            String[] fileNameArr2 = fileNamePath2.split("\\\\");
            fileName2 = fileNameArr2[fileNameArr2.length - 1];
            messagList2 = elList2.stream().map(TypeElement::name).collect(Collectors.toList());
            OptionElement packageData2 = pb2.options().get(0);
            pbPackPath2 = packageData2.value().toString();
            for (TypeElement e : elList2) {
                if (e instanceof EnumElement) {
                    EnumElement enumElement = (EnumElement) e;
                    List<EnumConstantElement> enmusList = enumElement.constants();
                    for (EnumConstantElement codeData : enmusList) {
                        int code = codeData.tag();
                        String cmdName = codeData.name();
                        String codePath = fileName2 + "." + enumElement.name() + "." + cmdName + "_VALUE";
                        elListMap2.put(code, codePath);

                        String[] cmdArr = cmdName.split("_");
                        StringBuilder cmdClass = new StringBuilder();
                        for (String s : cmdArr) {
                            cmdClass.append(s.substring(0, 1).toUpperCase() + s.substring(1));
                        }
                        elListCmdMap2.put(code, cmdClass.toString());
                    }
                }
            }
        }

        messagList = elList.stream().map(TypeElement::name).collect(Collectors.toList());
        String moduleName = fileName.substring(0, 1).toLowerCase() + fileName.substring(1);

        String modulePath = "GameServer\\src\\com\\yanqu\\xiuxian\\gameserver\\gameplayer\\module\\" + moduleName;
        // String path = ROOT_PATH + modulePath + "\\cmd\\";

        String packageModuleName = "com.yanqu.xiuxian.gameserver.gameplayer.module." + moduleName;


        boolean isCross = false;

        moduleName = moduleName.replace("cross", "");
        moduleName = moduleName.substring(0, 1).toLowerCase() + moduleName.substring(1);

        String packageModuleAcName = "com.yanqu.xiuxian.gameserver.gameplayer.module.activity." + moduleName;
        String serverAcPathName = packageModuleAcName + ".cmd";
        String moduleAcPath = "GameServer\\src\\com\\yanqu\\xiuxian\\gameserver\\gameplayer\\module\\activity\\" + moduleName;
        String serverAcPath = ROOT_PATH + moduleAcPath + "\\cmd\\";

        String packageModuleServerName = "com.yanqu.xiuxian.gameserver.gameplayer.module." + moduleName;
        String serverPathName = packageModuleServerName + ".cmd";
        String moduleServerPath = "GameServer\\src\\com\\yanqu\\xiuxian\\gameserver\\gameplayer\\module\\" + moduleName;
        String serverPath = ROOT_PATH + moduleServerPath + "\\cmd\\";


        if (pbFilePath.contains("crossproto")) {
            isCross = true;
            if (pbFilePath.contains("system")) {
                packageModuleName = "com.yanqu.xiuxian.crosssystem.manger." + moduleName;
                // packageName = "com.yanqu.xiuxian.crosssystem.manger." + moduleName + ".cmd";
                modulePath = "CrossSystem\\src\\com\\yanqu\\xiuxian\\crosssystem\\manger\\" + moduleName;
            } else {
                packageModuleName = "com.yanqu.xiuxian.crossactivity.manger." + moduleName;
                // packageName = "com.yanqu.xiuxian.crossactivity.manger." + moduleName + ".cmd";
                modulePath = "CrossActivity\\src\\com\\yanqu\\xiuxian\\crossactivity\\manger\\" + moduleName;
            }

        } else if (pbFilePath.contains("activity")) {
            packageModuleName = packageModuleAcName;
            modulePath = moduleAcPath;
        }
        String path = ROOT_PATH + modulePath + "\\cmd\\";


        String packageName = packageModuleName + ".cmd";

        for (TypeElement e : elList) {
            if (e instanceof EnumElement) {
                EnumElement enumElement = (EnumElement) e;
                List<EnumConstantElement> enmusList = enumElement.constants();
                for (EnumConstantElement codeData : enmusList) {
                    int code = codeData.tag();
                    String cmdName = codeData.name();
                    String cmdDesc = codeData.documentation().replace("\r", "");
                    if (cmdDesc.contains("skip")) {
                        continue;
                    }
                    String[] cmdArr = cmdName.split("_");
                    StringBuilder cmdClass = new StringBuilder();
                    for (String s : cmdArr) {
                        cmdClass.append(s.substring(0, 1).toUpperCase() + s.substring(1));
                    }
                    String codePath = fileName + "." + enumElement.name() + "." + cmdName + "_VALUE";
                    StringBuilder template;
                    boolean serverCmd = true;
                    if (code >= 30_0000 && code < 49_99999) {
                        serverCmd = false;
                    }
                    String finalPath = path;
                    String finalePackageName = packageName;
                    String finalModuelPath = modulePath;
                    finalPackageModuleName = packageModuleName;
                    if (serverCmd) {
                        if (isCross) {
                            if (pbFilePath.contains("activity")) {
                                finalPath = serverAcPath;
                                finalePackageName = serverAcPathName;
                                finalModuelPath = moduleAcPath;
                                finalPackageModuleName = packageModuleAcName;
                            } else {
                                finalPath = serverPath;
                                finalePackageName = serverPathName;
                                finalModuelPath = moduleServerPath;
                                finalPackageModuleName = packageModuleServerName;
                            }
                        }
                        if (cmdDesc.contains("mgr")) {
                            finalPath = finalPath.replace("gameplayer\\module", "manger");
                            finalePackageName = finalePackageName.replace("gameplayer.module", "manger");
                            finalModuelPath = finalModuelPath.replace("gameplayer\\module", "manger");
                            finalPackageModuleName = finalPackageModuleName.replace("gameplayer.module", "manger");
                        }
                    }


                    String fileCmd = cmdClass + "Cmd.java";
                    //来自跨服
                    if (serverCmd && isCross) {
                        fileCmd = "cross\\" + cmdClass + "Cmd.java";
                    }
                    String fileCmdPath = finalPath + fileCmd;
                    File file = new File(fileCmdPath);

                    File parentDir = file.getParentFile();
                    if (parentDir != null && !parentDir.exists()) {
                        parentDir.mkdirs();
                    }
                    //创建module
                    String moduleTmp;
                    if (isCross) {
                        moduleTmp = (code >= 30_0000 && code < 39_9999) ? "Activity" : "Mgr";
                    } else {
                        moduleTmp = cmdDesc.contains("mgr") ? "Mgr" : "Module";
                        if (pbFilePath.contains("activity") && cmdDesc.contains("mgr")) {
                            moduleTmp = "Activity";
                        }
                    }
                    //跨服到本服的协议不创建
                    if (!(isCross && serverCmd)) {
                        String moduleFile = ROOT_PATH + finalModuelPath + "\\" + fileName + moduleTmp + ".java";
                        File moduleFd = new File(moduleFile);
                        if (!moduleFd.exists()) {
                            createModule(moduleName, moduleFd, isCross, pbFilePath.contains("activity"), cmdDesc.contains("mgr"), code);
                        }
                    }

                    if (file.exists()) {
                        continue;
                    }

                    if (isCross) {
                        if (code >= 30_0000 && code < 49_99999) {
                            String serverCodeStr = "";
                            String serverCodeResp = "";
                            int serverCode = code >= 30_0000 && code < 39_9999 ? code - 10_0000 : code - 20_0000;
                            if (elListMap2.containsKey(serverCode)) {
                                serverCodeStr = elListMap2.get(serverCode);
                            }
                            if (elListCmdMap2.containsKey(serverCode)) {
                                serverCodeResp = elListCmdMap2.get(serverCode);
                            }
                            template = createCrossTemplate(cmdClass, finalePackageName, codePath, packageData.value().toString(), fileName, cmdDesc, code <= 39_9999, moduleName, serverCodeStr, serverCodeResp);
                        } else {
                            template = createTemplate(cmdClass, finalePackageName, codePath, packageData.value().toString(), fileName, cmdDesc, true, moduleName);
                        }
                    } else {
                        template = createTemplate(cmdClass, finalePackageName, codePath, packageData.value().toString(), fileName, cmdDesc, false, moduleName);
                    }
                    try (FileWriter fileWriter = new FileWriter(file)) {
                        fileWriter.write(String.valueOf(template));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    String logicPath = ROOT_PATH + finalModuelPath + "\\logic\\";
                    String originLogicPath = ROOT_PATH + finalModuelPath + "\\logic\\";
                    //来自跨服
                    boolean createBaseLogic = true;
                    String createLogicName = fileName;
                    if (serverCmd && isCross) {
                        createBaseLogic = false;
                        createLogicName = fileName2;
                    }

                    String logicPathFile = logicPath + cmdClass + "Logic.java";
                    String logicBasePathFile = originLogicPath + createLogicName + "BaseLogic.java";
                    createLogic(moduleName, cmdClass.toString(), logicPathFile, logicBasePathFile, isCross, pbFilePath.contains("activity"), cmdDesc.contains("mgr"), code, packageData.value().toString(), createBaseLogic);

                    System.out.println("create cmd success " + fileCmdPath);
                }
            }
        }

    }

    public static void createModule(String moduleName, File moduleFd, boolean isCross, boolean isAc, boolean isMgr, int code) throws IOException {
        String file;
        if (isCross) {
            if (code >= 30_0000 && code < 39_9999) {
                file = "./CrossAcvityMgr.txt";
            } else {
                file = "./CrossSystemMgr.txt";
            }
        } else {
            if (isMgr) {
                file = "./ServerPlayerMgr.txt";
                if (isAc) {
                    file = "./ServerPlayerAcvityMgr.txt";
                }
            } else {
                file = "./ServerPlayerModule.txt";
                if (isAc) {
                    file = "./ServerPlayerActivityModule.txt";
                }
            }
        }
        //File fileClass = new File(file);
        StringBuilder content = new StringBuilder();
        try (FileReader fileReader = new FileReader(file);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("{moduleName}")) {
                    line = line.replace("{moduleName}", moduleName);
                }
                if (line.contains("{moduleClass}")) {
                    line = line.replace("{moduleClass}", moduleName.substring(0, 1).toUpperCase() + moduleName.substring(1));
                }
                if (line.contains("{desc}")) {
                    line = line.replace("{desc}", "auto create");
                }
                if (line.contains("{author}")) {
                    line = line.replace("{author}", author);
                }
                if (line.contains("{date}")) {
                    line = line.replace("{date}", date);
                }
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (FileWriter fileWriter = new FileWriter(moduleFd)) {
            fileWriter.write(String.valueOf(content));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("create module success " + moduleFd);
    }

    public static StringBuilder createCrossTemplate(StringBuilder className, String packageName, String code, String pbPackage, String fileName, String codeDesc, boolean isActivity, String moduleName, String serverCode, String serverCodeResp) {

        String req = fileName + "." + className + "Req";
        if (!messagList.contains(className + "Req")) {
            if (isActivity) {
                req = "BaseCrossProto.BaseCrossReqMsg";

            } else {
                req = "Common.EmptyMsg";
            }
        }
        String resp = fileName + "." + className + "Resp";
        if (!messagList.contains(className + "Resp")) {
            if (isActivity) {
                resp = "BaseCrossProto.BaseCrossReqMsg";

            } else {
                resp = "Common.EmptyMsg";
            }
        }
        if (!serverCodeResp.isEmpty() && !messagList.contains(className + "Resp")) {
            resp = fileName2 + "." + serverCodeResp + "Resp";
        }
        String cmdTmlFile = isActivity ? "./CrossAcvityCmd.txt" : "./CrossSystemCmd.txt";

        StringBuilder content = new StringBuilder();

        String moduleClass = moduleName.substring(0, 1).toUpperCase() + moduleName.substring(1);
        String cmdClass = className.toString();
        String cmdFunc = cmdClass.substring(0, 1).toLowerCase() + cmdClass.substring(1);

        try (FileReader fileReader = new FileReader(cmdTmlFile);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("{moduleName}")) {
                    line = line.replace("{moduleName}", moduleName);
                }
                if (line.contains("{moduleClass}")) {
                    line = line.replace("{moduleClass}", moduleClass);
                }
                if (line.contains("{cmdClass}")) {
                    line = line.replace("{cmdClass}", cmdClass);
                }
                if (line.contains("{cmdFunc}")) {
                    line = line.replace("{cmdFunc}", cmdFunc);
                }
                if (line.contains("{cts}")) {
                    line = line.replace("{cts}", req);
                }
                if (line.contains("{stc}")) {
                    line = line.replace("{stc}", resp);
                }
                if (line.contains("{pbPath}")) {
                    line = line.replace("{pbPath}", pbPackage + "." + fileName);
                }
                if (line.contains("{code}")) {
                    line = line.replace("{code}", code);
                }
                if (line.contains("{desc}")) {
                    line = line.replace("{desc}", "auto create cmd");
                }
                if (line.contains("{author}")) {
                    line = line.replace("{author}", author);
                }
                if (line.contains("{date}")) {
                    line = line.replace("{date}", date);
                }
                if (line.contains("{cmdDesc}")) {
                    line = line.replace("{cmdDesc}", codeDesc);
                }
                if (line.contains("{serverCode}")) {
                    line = line.replace("{serverCode}", serverCode);
                }
                if (line.contains("{pbServerPath}")) {
                    line = line.replace("{pbServerPath}", pbPackPath2 + "." + fileName2);
                }
                //{cmdClass} {cmdFunc} {cts} {stc}
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    public static StringBuilder createTemplate(StringBuilder className, String packageName, String code, String pbPackage, String fileName, String codeDesc, boolean fromCross, String moduleName) {


        StringBuilder template = new StringBuilder();

        String ModuleClass = fileName.replace("Cross", "");

        String ActivityName = ModuleClass + "Activity";
        boolean isMgr = codeDesc.contains("mgr");
        boolean isAc = packageName.contains("activity");
        String req = fileName + "." + className + "Req";
        String resp = fileName + "." + className + "Resp";
        boolean isCommon = false;
        if (!messagList.contains(className + "Req")) {
            req = "Common.EmptyMsg";
            isCommon = true;
        }
        if (!messagList.contains(className + "Resp")) {
            resp = "Common.CommonResp";
            isCommon = true;
        }
        if (fromCross) {
            String cmdClass = className.toString();
            String className2;
            className2 = cmdClass.replace("Ctg", "Gtc");
            className2 = className2.replace("Back", "");
            String tmpResp = fileName + "." + className2 + "Resp";
            if (messagList.contains(className2 + "Resp")) {
                req = tmpResp;
                resp = tmpResp;
            } else {
                String className3;
                className3 = cmdClass.replace("Ctg", "");
                className3 = className3.replace("Back", "");
                String tmpResp2 = fileName2 + "." + className3 + "Resp";
                if (messagList2.contains(className3 + "Resp")) {
                    req = tmpResp2;
                    resp = tmpResp2;
                }
            }
        }
        String cmdTmlFile = "";
        if (!isAc) {
            cmdTmlFile = isMgr ? "./ServerMgrCmd.txt" : "./ServerModuleCmd.txt";

        } else {
            cmdTmlFile = isMgr ? "./ServerActivityMgrCmd.txt" : "./ServerActivityModuleCmd.txt";
        }
        if (cmdTmlFile.isEmpty()) {
            return template;
        }

        String clientCommand = fromCross ? "CrossToGameCommand" : "ClientCommand";

        String moduleClass = moduleName.substring(0, 1).toUpperCase() + moduleName.substring(1);
        String cmdClass = className.toString();
        String cmdFunc = cmdClass.substring(0, 1).toLowerCase() + cmdClass.substring(1);
        StringBuilder content = new StringBuilder();
        String cross = "";
        try (FileReader fileReader = new FileReader(cmdTmlFile);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("{moduleName}")) {
                    line = line.replace("{moduleName}", moduleName);
                }
                if (line.contains("{moduleClass}")) {
                    line = line.replace("{moduleClass}", moduleClass);
                }
                if (line.contains("{cmdClass}")) {
                    line = line.replace("{cmdClass}", cmdClass);
                }
                if (line.contains("{cmdFunc}")) {
                    line = line.replace("{cmdFunc}", cmdFunc);
                }
                if (line.contains("{cts}")) {
                    line = line.replace("{cts}", req);
                }
                if (line.contains("{stc}")) {
                    line = line.replace("{stc}", resp + ".Builder");
                }
                if (line.contains("{pbPath}")) {
                    line = line.replace("{pbPath}", pbPackage + "." + fileName);
                }
                if (line.contains("{code}")) {
                    line = line.replace("{code}", code);
                }
                if (line.contains("{desc}")) {
                    line = line.replace("{desc}", "auto create cmd");
                }
                if (line.contains("{author}")) {
                    line = line.replace("{author}", author);
                }
                if (line.contains("{date}")) {
                    line = line.replace("{date}", date);
                }
                if (line.contains("{cmdDesc}")) {
                    line = line.replace("{cmdDesc}", codeDesc);
                }
                if (line.contains("{cross}")) {
                    if (fromCross) {
                        cross = ".cross";
                    }
                    line = line.replace("{cross}", cross);
                }
                if (line.contains("{clientCommand}")) {
                    line = line.replace("{clientCommand}", clientCommand);
                }
                if (line.contains("{pbPath2}")) {
                    String pbPath2 = "";
                    if (fromCross) {
                        pbPath2 = "import " + pbPackage.replace("cross.", "");
                        pbPath2 = pbPath2 + "." + ModuleClass + ";";
                    }
                    line = line.replace("{pbPath2}", pbPath2);
                }
                //{cmdClass} {cmdFunc} {cts} {stc}
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    public static void execPb() {
       /* try {
            String execFile = ROOT_PATH + "PBProtocol\\protocol2.bat";
            // 假设你的exec文件路径是 "C:\\path\\to\\your\\executable.exe"
            Process process = Runtime.getRuntime().exec(execFile);

            // 读取exec文件的输出（如果有的话）
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // 等待exec文件执行完成
            process.waitFor();

            // 获取exec文件的退出值
            int exitValue = process.exitValue();
            System.out.println("Exit value: " + exitValue);

            // 创建Robot对象
            Robot robot = new Robot();

            // 模拟按下Enter键
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }*/
    }

    public static void createLogic(String moduleName, String cmdClass, String logicPathFile, String logicBasePathFile, boolean isCross, boolean isAc, boolean isMgr, int code, String pbPath, boolean createBaseLogic) {
        File fileFd = new File(logicPathFile);

        File parentDir = fileFd.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        if (fileFd.exists()) {
            return;
        }
        String file;
        String baseFile = "";
        boolean createCrossLogic = isCross && createBaseLogic;
        if (createCrossLogic) {
            if (code >= 30_0000 && code < 39_9999) {
                file = "./CrossAcvityLogic.txt";
                baseFile = "./CrossAcvityBaseLogic.txt";
            } else {
                file = "./CrossSystemLogic.txt";
                baseFile = "./CrossSystemBaseLogic.txt";
            }
        } else {
            if (isMgr) {
                file = "./ServerModuleMgrLogic.txt";
                baseFile = "./ServerModuleMgrBaseLogic.txt";
                if (isAc) {
                    file = "./ServerActivityLogic.txt";
                    baseFile = "./ServerActivityBaseLogic.txt";
                }
            } else {
                file = "./ServerModuleLogic.txt";
                baseFile = "./ServerModuleBaseLogic.txt";
                if (isAc) {
                    file = "./ServerActivityModuleLogic.txt";
                    baseFile = "./ServerActivityModuleBaseLogic.txt";
                }
            }
        }
        String moduleClass = moduleName.substring(0, 1).toUpperCase() + moduleName.substring(1);
        if (!baseFile.isEmpty()) {
            File baseLogicFd = new File(logicBasePathFile);
            if (!baseLogicFd.exists()) {
                StringBuilder baseContent = new StringBuilder();
                try (FileReader fileReader = new FileReader(baseFile);
                     BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        if (line.contains("{moduleName}")) {
                            line = line.replace("{moduleName}", moduleName);
                        }
                        if (line.contains("{moduleClass}")) {
                            line = line.replace("{moduleClass}", moduleClass);
                        }
                        if (line.contains("{pbPath}")) {
                            line = line.replace("{pbPath}", pbPath + "." + fileName);
                        }
                        if (line.contains("{desc}")) {
                            line = line.replace("{desc}", "auto create");
                        }
                        if (line.contains("{author}")) {
                            line = line.replace("{author}", author);
                        }
                        if (line.contains("{date}")) {
                            line = line.replace("{date}", date);
                        }
                        if (line.contains("{pbServerPath}")) {
                            line = line.replace("{pbServerPath}", pbPackPath2 + "." + fileName2);
                        }
                        baseContent.append(line).append("\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try (FileWriter fileWriter = new FileWriter(baseLogicFd)) {
                    fileWriter.write(String.valueOf(baseContent));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                System.out.println("create logic success " + logicBasePathFile);
            }
        }

        String req = fileName + "." + cmdClass + "Req";
        String resp = fileName + "." + cmdClass + "Resp";
        boolean isCommon = false;
        if (!messagList.contains(cmdClass + "Req")) {
            req = "Common.EmptyMsg";
            isCommon = true;
        }
        if (!messagList.contains(cmdClass + "Resp")) {
            resp = "Common.CommonResp";
            isCommon = true;
        }
        //来自跨服
        if (!createCrossLogic && isCross) {
            // req = fileName + "." + cmdClass + "Req";
            String className2;
            className2 = cmdClass.replace("Ctg", "Gtc");
            className2 = className2.replace("Back", "");
            String tmpResp = fileName + "." + className2 + "Resp";
            if (messagList.contains(className2 + "Resp")) {
                req = tmpResp;
                resp = tmpResp;
            } else {
                String className3;
                className3 = cmdClass.replace("Ctg", "");
                className3 = className3.replace("Back", "");
                String tmpResp2 = fileName2 + "." + className3 + "Resp";
                if (messagList2.contains(className3 + "Resp")) {
                    req = tmpResp2;
                    resp = tmpResp2;
                }
            }
        }
        boolean createPb2 = false;
        if (createBaseLogic && !messagList.contains(cmdClass + "Resp")) {
            String serverCodeStr = "";
            String serverCodeResp = "";
            int serverCode = code >= 30_0000 && code < 39_9999 ? code - 10_0000 : code - 20_0000;
            if (elListMap2.containsKey(serverCode)) {
                serverCodeStr = elListMap2.get(serverCode);
            }
            if (elListCmdMap2.containsKey(serverCode)) {
                serverCodeResp = elListCmdMap2.get(serverCode);
            }
            if (!serverCodeResp.isEmpty()) {
                resp = fileName2 + "." + serverCodeResp + "Resp";
                createPb2 = true;
            }
        }

        //File fileClass = new File(file);
        StringBuilder content = new StringBuilder();
        try (FileReader fileReader = new FileReader(file);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("{moduleName}")) {
                    line = line.replace("{moduleName}", moduleName);
                }
                if (line.contains("{moduleClass}")) {
                    line = line.replace("{moduleClass}", moduleClass);
                }
                if (line.contains("{cmdClass}")) {
                    line = line.replace("{cmdClass}", cmdClass);
                }
                if (line.contains("{cmdFunc}")) {
                    line = line.replace("{cmdFunc}", cmdClass.substring(0, 1).toLowerCase() + cmdClass.substring(1));
                }
                if (line.contains("{cts}")) {
                    line = line.replace("{cts}", req);
                }
                if (line.contains("{stc}")) {
                    line = line.replace("{stc}", resp + ".Builder");
                }
                if (line.contains("{pbPath}")) {
                    line = line.replace("{pbPath}", pbPath + "." + fileName);
                }
                if (line.contains("{desc}")) {
                    line = line.replace("{desc}", "auto create");
                }
                if (line.contains("{pbPath2}")) {
                    String pbPath2 = "";
                    if (!createCrossLogic || createPb2) {
                        pbPath2 = "import " + pbPath.replace("cross.", "");
                        String ModuleClass = fileName.replace("Cross", "");
                        pbPath2 = pbPath2 + "." + ModuleClass + ";";
                    }
                    line = line.replace("{pbPath2}", pbPath2);
                }
                if (line.contains("{author}")) {
                    line = line.replace("{author}", author);
                }
                if (line.contains("{date}")) {
                    line = line.replace("{date}", date);
                }
                if (line.contains("{pbServerPath}")) {
                    line = line.replace("{pbServerPath}", pbPackPath2 + "." + fileName2);
                }
                //{cmdClass} {cmdFunc} {cts} {stc}
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        File logicFd = new File(logicPathFile);
        try (FileWriter fileWriter = new FileWriter(logicFd)) {
            fileWriter.write(String.valueOf(content));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("create logic success " + logicFd);

    }

}

