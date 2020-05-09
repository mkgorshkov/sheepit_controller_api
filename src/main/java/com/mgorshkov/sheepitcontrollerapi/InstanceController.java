package com.mgorshkov.sheepitcontrollerapi;

import com.mgorshkov.common.ProcessLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

@RestController
public class InstanceController {

    private static final Logger logger = LoggerFactory.getLogger(InstanceController.class);
    private HashMap<ProcessLabel, String> sheepitProcess;

    @GetMapping("healthcheck")
    public String healthCheck(HttpServletRequest request){
        String baseUrl = String.format("%s://%s:%d/",request.getScheme(),  request.getServerName(), request.getServerPort());
        return checkProcessRunning(baseUrl);
    }

    @GetMapping("killInstance/{pid}")
    public String killInstance(@PathVariable String pid){
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("sh", "-c", "kill 9 "+pid);
        try {
            Process process = builder.start();
            int exitCode = process.waitFor();
            if(exitCode == 0){
                return "Successfully killed Sheepit process";
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }

        return "Error occurred, could not kill Sheepit process";
    }

    @GetMapping("restartInstance/{pid}")
    public String restartInstance(HttpServletRequest request, @PathVariable String pid){
        String baseUrl = String.format("%s://%s:%d/",request.getScheme(),  request.getServerName(), request.getServerPort());

        killInstance(pid);

        ProcessBuilder builder = new ProcessBuilder();
        builder.command("sh", "-c", sheepitProcess.get(ProcessLabel.CMD));
        try {
            Process process = builder.start();
            return "Killed old Sheepit process. Successfully started new Sheepit process."+
                    " <a href=\""+baseUrl+"healthcheck/\">Jump to HealthCheck</a>";
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        return "Couldn't restart instance";
    }

    private String checkProcessRunning(String baseURL) {
        String errorText = "No instances of Sheepit running.";
        String validOutput = "";

        String match = "sheepit-client";
        String pidLine = "";

        try {
            String line;
            Process p = Runtime.getRuntime().exec("ps -e -o pid,utime,command");
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
        }else{
            return errorText;
        }

        return "Currently running an instance of Sheepit with PID "+sheepitProcess.get(ProcessLabel.PID)+" and UPTIME "+sheepitProcess.get(ProcessLabel.UPTIME)+"." +
                " Options: <a href=\""+baseURL+"restartInstance/"+sheepitProcess.get(ProcessLabel.PID)+"\"> RESTART Instance</a> | " +
                " <a href=\""+baseURL+"killInstance/"+sheepitProcess.get(ProcessLabel.PID)+"\">KILL Instance</a>";
    }
}
