package com.mgorshkov.sheepitcontrollerapi;

import com.mgorshkov.common.ProcessLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

@Controller
public class InstanceController {

    private static final Logger logger = LoggerFactory.getLogger(InstanceController.class);
    private HashMap<ProcessLabel, String> sheepitProcess;

    /**
     * This endpoint will perform a healthcheck on the instance running sheepit by doing a process command
     * @param request - will be used to get baseURL
     * @return String to show to end-user
     */
    @GetMapping("healthcheck")
    public String healthCheck(HttpServletRequest request, Model model){
        String baseUrl = String.format("%s://%s:%d/",request.getScheme(),  request.getServerName(), request.getServerPort());
        checkProcessRunning(baseUrl);

        if(sheepitProcess == null){
            model.addAttribute("no_sheepit", "No instances of Sheepit running.");
        }else{
            model.addAttribute("process_pid", sheepitProcess.get(ProcessLabel.PID));
            model.addAttribute("process_uptime", sheepitProcess.get(ProcessLabel.UPTIME));
            model.addAttribute("restart_url", sheepitProcess.get(ProcessLabel.RESTART_URL));
            model.addAttribute("kill_url", sheepitProcess.get(ProcessLabel.KILL_URL));
        }

        return "healthcheck";
    }

    /**
     * This endpoint will kill an instance of sheepit with a specific PID as retrieved through a healthcheck
     * @param pid - get a processID from a healthcheck or passed directly to the service otherwise
     * @return String to show to end-user
     */
    @GetMapping("killInstance/{pid}")
    public String killInstance(@PathVariable String pid, Model model){
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("sh", "-c", "kill 9 "+pid);
        try {
            Process process = builder.start();
            int exitCode = process.waitFor();
            if(exitCode == 0){
                sheepitProcess = null;

                model.addAttribute("status_message", "Successfully killed Sheepit process");
                model.addAttribute("available_operations", "true");
                return "instanceoperation";
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }

        model.addAttribute("status_message", "Error occurred, could not kill Sheepit process");
        return "instanceoperation";
    }

    /**
     * Restarts a process of sheepit on a server. Will first kill an existing instance given the PID and restarts with
     * the same command so it's run with the same username/password/etc.
     * @param request - to be used for baseURL
     * @param pid - process to kill
     * @return String to show to end-user
     */
    @GetMapping("restartInstance/{pid}")
    public String restartInstance(HttpServletRequest request, @PathVariable String pid, Model model){
        String baseUrl = String.format("%s://%s:%d/",request.getScheme(),  request.getServerName(), request.getServerPort());

        String saved_command = sheepitProcess.get(ProcessLabel.CMD);

        killInstance(pid, model);

        ProcessBuilder builder = new ProcessBuilder();
        builder.command("sh", "-c", saved_command);
        try {
            Process process = builder.start();
            model.addAttribute("status_message", "Killed old Sheepit process. Successfully started new Sheepit process.");
            model.addAttribute("available_operations", "true");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        return "instanceoperation";
    }

    /**
     * Will perform the process check and fill a map with a few of the items.
     * @param baseURL - used to send to a different page
     * @return String to show to end-user
     */
    private String checkProcessRunning(String baseURL) {
        String errorText = "No instances of Sheepit running.";
        String validOutput = "";

        String match = "sheepit-client";
        String pidLine = "";

        try {
            String line;
            Process p = Runtime.getRuntime().exec("ps -e -o pid,time,command");
            BufferedReader input =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                if(line.contains(match)){
                    pidLine = line;
                }
            }
            input.close();
        } catch (Exception err) {
            logger.error(err.getMessage());
        }

        if(pidLine.length() > 1){
            sheepitProcess = new HashMap<>();

            String[] splitPIDLine = pidLine.split("   ");
            sheepitProcess.put(ProcessLabel.PID, splitPIDLine[0]);

            int index = splitPIDLine[1].indexOf(" ");
            sheepitProcess.put(ProcessLabel.UPTIME, splitPIDLine[1].substring(0,index));
            sheepitProcess.put(ProcessLabel.CMD, splitPIDLine[1].substring(index));
            sheepitProcess.put(ProcessLabel.RESTART_URL, baseURL+"restartInstance/"+sheepitProcess.get(ProcessLabel.PID));
            sheepitProcess.put(ProcessLabel.KILL_URL, baseURL+"killInstance/"+sheepitProcess.get(ProcessLabel.PID));
        }

        return errorText;
    }
}
