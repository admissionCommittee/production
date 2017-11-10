package com.github.admissionCommittee.service;

import com.github.admissionCommittee.dao.DaoFactory;
import com.github.admissionCommittee.model.Faculty;
import com.github.admissionCommittee.model.Sheet;
import com.github.admissionCommittee.dao.FacultyDao;
import com.github.admissionCommittee.dao.SheetDao;

import java.util.*;

public class SheetService extends GenericService<Sheet> {
    public SheetService() {
        super(Sheet.class, DaoFactory.getSheetDao());
    }

    public List<Sheet> getByFaculty(Faculty faculty) {
        return ((SheetDao) getDao()).getByFaculty(faculty);
    }

    //get only attendees that satisfy the attendee demands
    public Map<Faculty, Map<Integer, Sheet>> getApprovedAttendees() {
        Map<Faculty, Map<Integer, Sheet>> approvedMap = new HashMap<>();
        List<Faculty> facultyList = new FacultyService()
                .getAll();
        facultyList.sort(Comparator.comparing(Faculty::getName));
        facultyList.parallelStream().forEachOrdered(faculty -> {
            approvedMap.put(faculty, getApprovedSheets(getByFaculty(faculty),
                    faculty.getPeopleLimit()));
        });
        return approvedMap;
    }

    public Map<Integer, Sheet> getApprovedSheets(List<Sheet> sheetList,
                                                 Integer attendeesLimit) {
        //getApprovedSheets - that belonging to successful attendees
        Map<Integer, Sheet> userMap = new TreeMap<>();
        sheetList.parallelStream().forEachOrdered(element -> {
            if (userMap.get(element.getSumExamCertificateScore())
                    == null) {
                userMap.put(element.getSumExamCertificateScore(),
                        element);
            } else {
                if (element.getAverageSchoolCertificateScore() > userMap
                        .get(element.getSumExamCertificateScore())
                        .getAverageSchoolCertificateScore()) {
                    userMap.put(element.getSumExamCertificateScore(),
                            element);
                }
            }
        });

        //check attendees limit and delete others
        Iterator iterator = userMap.entrySet().iterator();
        int counter = 0;
        while (iterator.hasNext()) {
            if (counter > attendeesLimit) {
                iterator.remove();
                counter++;
            }
        }
        return userMap;
    }
}