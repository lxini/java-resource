package com.lagou.edu.service;

import com.lagou.edu.pojo.Resume;

import java.util.List;

public interface ResumeService {
    List<Resume> queryResumeList() throws Exception;

    Resume queryResumeById(Integer id);

    void updateResumeById(Resume resume);

    void deleteResumeById(long longValue);
}
