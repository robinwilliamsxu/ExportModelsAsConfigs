/**
 * Copyright 2014 Mentor Graphics Corporation. All Rights Reserved.
 * <p>
 * Recipients who obtain this code directly from Mentor Graphics use it solely
 * for internal purposes to serve as example Java or JavaScript plugins.
 * This code may not be used in a commercial distribution. Recipients may
 * duplicate the code provided that all notices are fully reproduced with
 * and remain in the code. No part of this code may be modified, reproduced,
 * translated, used, distributed, disclosed or provided to third parties
 * without the prior written consent of Mentor Graphics, except as expressly
 * authorized above.
 * <p>
 * THE CODE IS MADE AVAILABLE "AS IS" WITHOUT WARRANTY OR SUPPORT OF ANY KIND.
 * MENTOR GRAPHICS OFFERS NO EXPRESS OR IMPLIED WARRANTIES AND SPECIFICALLY
 * DISCLAIMS ANY WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE,
 * OR WARRANTY OF NON-INFRINGEMENT. IN NO EVENT SHALL MENTOR GRAPHICS OR ITS
 * LICENSORS BE LIABLE FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING LOST PROFITS OR SAVINGS) WHETHER BASED ON CONTRACT, TORT
 * OR ANY OTHER LEGAL THEORY, EVEN IF MENTOR GRAPHICS OR ITS LICENSORS HAVE BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * <p>
 */
 
import com.mentor.chs.api.IXIntegratorDesign;
import com.mentor.chs.api.IXOption;
import com.mentor.chs.api.IXProject;
import com.mentor.chs.api.IXVehicleModel;
import com.mentor.chs.plugin.IXApplicationContext;
import com.mentor.chs.plugin.IXOutputWindow;
import com.mentor.chs.plugin.action.IXIntegratorAction;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by sholdswo on 01/09/14.
 */
public class ExportModelsAsConfigs implements IXIntegratorAction
{
    private JFileChooser m_fc;

    public ExportModelsAsConfigs()
    {
        m_fc = new JFileChooser();
        m_fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

        //File dir = FileSystemView.getFileSystemView().getDefaultDirectory();
        m_fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        m_fc.setDialogTitle(getName() + ": Select output file");
    }

    public boolean execute(IXApplicationContext ixApplicationContext)
    {
        if (ixApplicationContext.getCurrentDesign() instanceof IXIntegratorDesign) {
            IXOutputWindow ow = ixApplicationContext.getOutputWindow();
            IXProject project = ixApplicationContext.getCurrentProject();
            IXIntegratorDesign design = (IXIntegratorDesign)ixApplicationContext.getCurrentDesign();

            m_fc.setSelectedFile(new File(design.getAttribute("Name")+".xml"));
            int returnVal = m_fc.showSaveDialog(ixApplicationContext.getParentFrame());
            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                File file = m_fc.getSelectedFile();
                try {
                    PrintWriter outFile = new PrintWriter(file);
                    outFile.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                    outFile.println("<!DOCTYPE optionmgr PUBLIC \"-//Mentor Graphics Corporation//OptionMgr 1.0//EN\" \"C:/CHS/chs_home/dtd/options.dtd\">");
                    outFile.println("<optionmgr id=\"_1\" type=\"options\" modified=\"1\" baseid=\"_1\">");
                    HashMap<IXOption,String> optionMap = new HashMap<IXOption,String>();
                    int id = 0;
                    for (IXOption opt : project.getOptions()) {
                        id++;
                        String uid = "_opt"+id;
                        optionMap.put(opt,uid);
                        outFile.print("<option id=\"" + uid + "\" name=\"" + opt.getName() + "\" nameindex=\"-1\"");
                        if (isVariant(opt)) {
                            outFile.println(" isvariant=\"true\" obsolete=\"false\"/>");
                        } else {
                            outFile.println(" isvariant=\"false\" obsolete=\"false\"/>");
                        }
                    }
                    id = 0;
                    for (IXVehicleModel vm : design.getVehicleModels()) {
                        id++;
                        String uid = "_cfg"+id;
                        outFile.println("<optionconfiguration id=\"" + uid + "\" name=\"" + vm.getAttribute("Name") + "\" description=\"\" baseid=\"" + uid + "\">");
                        for (IXOption opt : vm.getSupportedOptions()) {
                            outFile.println("<refedoption optionref=\"" + optionMap.get(opt) + "\"/>");
                        }
                        outFile.println("</optionconfiguration>");
                    }
                    outFile.println("</optionmgr>");
                    outFile.close();
                    ow.println("Configuration XML written to file: " + file.getName());
                } catch (IOException e) {
                    ow.println("Failed to create XML file: " + file.getName());
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isVariant(IXOption option) {
        if (option != null) {
            String var = option.getAttribute("Variant");
            return (var!=null && var.toLowerCase().startsWith("t"));
        }
        return false;
    }

    public Trigger[] getTriggers() {
        return new Trigger[]{Trigger.MainMenu};
    }

    public Icon getSmallIcon() {
        return null;
    }

    public Integer getMnemonicKey() {
        return null;
    }

    public String getLongDescription() {
        return getDescription();
    }

    public boolean isReadOnly() {
        return true;
    }

    public String getDescription() {
        return "Generate option configuration XML from Vehicle Models";
    }

    public String getName() {
        return "Export Models as Configurations";
    }

    public String getVersion() {
        return "0.1";
    }

    public boolean isAvailable(IXApplicationContext ixApplicationContext) {
        return true;
    }
}
