package com.udacity.jwdnd.course1.cloudstorage.services;

import com.udacity.jwdnd.course1.cloudstorage.form.CredentialForm;
import com.udacity.jwdnd.course1.cloudstorage.form.HomeForm;
import com.udacity.jwdnd.course1.cloudstorage.mapper.CredentialsMapper;
import com.udacity.jwdnd.course1.cloudstorage.mapper.FilesMapper;
import com.udacity.jwdnd.course1.cloudstorage.mapper.NotesMapper;
import com.udacity.jwdnd.course1.cloudstorage.model.Credentials;
import com.udacity.jwdnd.course1.cloudstorage.model.Files;
import com.udacity.jwdnd.course1.cloudstorage.model.Notes;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HomeService {

    FilesMapper filesMapper;
    NotesMapper notesMapper;
    CredentialsMapper credentialsMapper;
    EncryptionService encryptionService;
    private String key = "";

    public HomeService(FilesMapper filesMapper, NotesMapper notesMapper, CredentialsMapper credentialsMapper, EncryptionService encryptionService) {
        this.filesMapper = filesMapper;
        this.notesMapper = notesMapper;
        this.credentialsMapper = credentialsMapper;
        this.encryptionService = encryptionService;
    }

    public int addNote(HomeForm homeForm) {
        Notes newNote = new Notes(homeForm.getNoteTitle(), homeForm.getNoteDescription(), homeForm.getUserId());
        return notesMapper.insertNotes(newNote);
    }

    public int updateNote(HomeForm homeForm) {
        return notesMapper.updateNotes(homeForm.getNoteId(), homeForm.getUserId(),
                homeForm.getNoteTitle(), homeForm.getNoteDescription());
    }

    public int deleteNote(int noteId, int userid) {
        return notesMapper.deleteNotes(noteId, userid);
    }

    public List<Notes> getAllNotes(int userId) {
        return notesMapper.findAllNotesByUserId(userId);
    }

    public int addCredentials(CredentialForm credentialForm) {
        return credentialsMapper.insertCredentials(createCredentials(credentialForm));
    }

    public int updateCredential(CredentialForm credentialForm) {
        return credentialsMapper.updateCredential(createCredentials(credentialForm));
    }

    public int deleteCredential(int credentialid, int userid) {
        return credentialsMapper.deleteCredential(credentialid, userid);
    }

    public int addFiles(Files files) {
        return filesMapper.insertFiles(files);
    }

    public List<Files> getAllFiles(int userid) {
        return filesMapper.getAllFilesByUserId(userid);
    }

    public int deleteFile(int fileid, int userid) {
        return filesMapper.deleteFiles(fileid, userid);
    }

    public Files findFile(int userid, int fileid) {
        return filesMapper.findFileByFileId(userid, fileid);
    }

    public boolean doesFileExist(int userId, String fileName) {
        int doesExist = filesMapper.doesFileExist(userId, fileName);
        return doesExist == 1;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    private Credentials createCredentials(CredentialForm credentialForm) {
        Credentials newCredential = new Credentials();
        if (credentialForm.getCredentialId() != 0)
            newCredential.setCredentialid(credentialForm.getCredentialId());
        newCredential.setKey(key);
        String encryptedPassword = encryptionService.encryptValue(credentialForm.getPassword(), newCredential.getKey());
        newCredential.setPassword(encryptedPassword);
        newCredential.setUrl(credentialForm.getUrl());
        newCredential.setUsername(credentialForm.getUsername());
        newCredential.setUserid(credentialForm.getUserId());
        return newCredential;
    }

    public List<ExtCredentials> getAllCredentials(int userid) {
        List<Credentials> credentialsList = credentialsMapper.getAllCredentialsByUserId(userid);
        List<ExtCredentials> extendedCredentialsList = new ArrayList();
        for (Credentials credentials : credentialsList) {
            String decrptyPassword = encryptionService.decryptValue(credentials.getPassword(), key);
            ExtCredentials extendedCredentials = new ExtCredentials(credentials);
            extendedCredentials.setDecryptedPassword(decrptyPassword);
            extendedCredentialsList.add(extendedCredentials);
        }
        return extendedCredentialsList;
    }
}
