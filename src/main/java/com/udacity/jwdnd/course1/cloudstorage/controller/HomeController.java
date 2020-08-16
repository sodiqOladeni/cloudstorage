package com.udacity.jwdnd.course1.cloudstorage.controller;

import com.udacity.jwdnd.course1.cloudstorage.form.CredentialForm;
import com.udacity.jwdnd.course1.cloudstorage.form.HomeForm;
import com.udacity.jwdnd.course1.cloudstorage.model.Files;
import com.udacity.jwdnd.course1.cloudstorage.model.Notes;
import com.udacity.jwdnd.course1.cloudstorage.model.User;
import com.udacity.jwdnd.course1.cloudstorage.services.EncryptionService;
import com.udacity.jwdnd.course1.cloudstorage.services.ExtCredentials;
import com.udacity.jwdnd.course1.cloudstorage.services.HomeService;
import com.udacity.jwdnd.course1.cloudstorage.services.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
public class HomeController {

    UserService userService;
    HomeService homeService;
    EncryptionService encryptionService;
    List<Notes> allNotes;
    List<ExtCredentials> allCredentials;
    List<Files> allFiles;

    public HomeController(UserService userService, HomeService homeService, EncryptionService encryptionService) {
        this.userService = userService;
        this.homeService = homeService;
        this.encryptionService = encryptionService;
    }

    @RequestMapping("/home")
    public String getHomePage(HomeForm homeForm, Model model) {
        allNotes = homeService.getAllNotes(getUserId());
        allCredentials = homeService.getAllCredentials(getUserId());
        allFiles = homeService.getAllFiles(getUserId());
        model.addAttribute("allNotes", allNotes);
        model.addAttribute("allCredentials", allCredentials);
        model.addAttribute("allFiles", allFiles);
        return "home";
    }

    @RequestMapping("/addOrUpdateNote")
    public String addOrUpdateNote(HomeForm homeForm, Model model) {
        homeForm.setUserId(getUserId());
        int success;
        if (homeForm.getNoteAction().equals("addNote")) {
            success = homeService.addNote(homeForm);
        } else {
            success = homeService.updateNote(homeForm);
        }
        model.addAttribute("resultSuccess", success == 1);
        model.addAttribute("tab", "notes");
        model.addAttribute("nextAction", "home");
        return "result";
    }

    @RequestMapping("/deletenote")
    public String deleteNote(@RequestParam String noteid, Model model) {
        int id = Integer.parseInt(noteid);
        int userid = getUserId();
        int success = homeService.deleteNote(id, userid);
        model.addAttribute("resultSuccess", success == 1);
        return "result";
    }

    @RequestMapping("/addOrUpdateCredentials")
    public String addOrUpdateCredentials(CredentialForm credentialForm, Model model) {
        credentialForm.setUserId(getUserId());
        int success;
        if (credentialForm.getCredentialAction().equals("addCredential")) {
            success = homeService.addCredentials(credentialForm);
        } else {
            success = homeService.updateCredential(credentialForm);
        }
        model.addAttribute("resultSuccess", success == 1);
        model.addAttribute("tab", "credential");
        model.addAttribute("nextAction", "home");
        return "result";
    }

    @RequestMapping("/deleteCredential")
    public String deleteCredentials(@RequestParam String credentialid, Model model) {
        int id = Integer.parseInt(credentialid);
        int userid = getUserId();
        int success = homeService.deleteCredential(id, userid);
        model.addAttribute("resultSuccess", success == 1);
        return "result";
    }

    @PostMapping("/fileupload")
    public String uploadFile2(@RequestParam("fileUpload") MultipartFile file, Model model) throws IOException {
        int success = 1;
        // check if file is empty
        if (file.isEmpty()) {
            model.addAttribute("resultErrorMsg", "File is empty.");
            model.addAttribute("resultError", true);
            return "result";
        }

        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        if (homeService.doesFileExist(getUserId(), fileName)) {
            model.addAttribute("resultErrorMsg", "File already exists.");
            model.addAttribute("resultError", true);
            return "result";
        }

        if (file.getSize() >= 1000000) {
            model.addAttribute("resultErrorMsg", "File size is too large");
            model.addAttribute("resultError", true);
            return "result";
        }

        Files files = new Files();
        // normalize the file paterrorh
        files.setFilename(fileName);
        files.setFilesize("" + file.getSize());
        files.setContenttype(file.getContentType());
        files.setUserid(getUserId());
        files.setFiledata(file.getBytes());
        success = homeService.addFiles(files);
        model.addAttribute("resultSuccess", success == 1);
        return "result";
    }

    @RequestMapping("/deletefile")
    public String deleteFile(@RequestParam String fileid, Model model) {
        int id = Integer.parseInt(fileid);
        int userid = getUserId();
        int success = homeService.deleteFile(id, userid);
        model.addAttribute("resultSuccess", success == 1);
        return "result";
    }

    @RequestMapping(value="/viewFile", method= RequestMethod.GET)
    public ResponseEntity<byte[]> handleViewFile(@RequestParam String fileid, Model model) {
        Files file = homeService.findFile(getUserId(), Integer.parseInt(fileid));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(file.getContenttype()));
        headers.add("Content-Disposition", "inline; filename=" + file.getFilename());
        headers.setContentDispositionFormData(file.getFilename(), file.getFilename());
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(file.getFiledata(), headers, HttpStatus.OK);
        return response;
    }

    private int getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            User user = userService.getUser(authentication.getName());
            return user.getUserid();
        }
        return 0;
    }
}
