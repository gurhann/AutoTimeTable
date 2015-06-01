/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.optaplanner.examples.curriculumcourse.persistence;

import java.io.File;
import org.optaplanner.examples.common.persistence.AbstractTxtSolutionImporter.TxtInputBuilder;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.examples.common.persistence.AbstractTxtSolutionImporter;
import org.optaplanner.examples.curriculumcourse.domain.Course;
import org.optaplanner.examples.curriculumcourse.domain.CourseSchedule;
import org.optaplanner.examples.curriculumcourse.domain.Curriculum;
import org.optaplanner.examples.curriculumcourse.domain.Day;
import org.optaplanner.examples.curriculumcourse.domain.Lecture;
import org.optaplanner.examples.curriculumcourse.domain.Period;
import org.optaplanner.examples.curriculumcourse.domain.Room;
import org.optaplanner.examples.curriculumcourse.domain.Teacher;
import org.optaplanner.examples.curriculumcourse.domain.Timeslot;
import org.optaplanner.examples.curriculumcourse.domain.UnavailablePeriodPenalty;

public class CurriculumCourseImporter extends AbstractTxtSolutionImporter {

    private static final String INPUT_FILE_SUFFIX = "ctt";
    private static final String SPLIT_REGEX = "[\\ \\t]+";

    public static void main(String[] args) {
        new CurriculumCourseImporter().convertAll();
    }

    public CurriculumCourseImporter() {
        super(new CurriculumCourseDao());
    }

    @Override
    public String getInputFileSuffix() {
        return INPUT_FILE_SUFFIX;
    }

    public TxtInputBuilder createTxtInputBuilder() {
        return new CurriculumCourseInputBuilder();
    }

    public static class CurriculumCourseInputBuilder extends TxtInputBuilder {

        public Solution readSolution() throws IOException {

            CourseSchedule schedule = new CourseSchedule();
            schedule.setId(0L);
            // Name: ToyExample
            schedule.setName(readStringValue("Name:"));
            // Courses: 4
            int courseListSize = readIntegerValue("Courses:");
            // Rooms: 2
            int roomListSize = readIntegerValue("Rooms:");
            // Days: 5
            int dayListSize = readIntegerValue("Days:");
            // Periods_per_day: 4
            int timeslotListSize = readIntegerValue("Periods_per_day:");
            // Curricula: 2
            int curriculumListSize = readIntegerValue("Curricula:");
            // Constraints: 8
            int unavailablePeriodPenaltyListSize = readIntegerValue("Constraints:");
            // Teacher Day Constraints:
            int teacherPenaltyListSize = readIntegerValue("TeacherDayConstraints:");

            Map<String, Course> courseMap = readCourseListAndTeacherList(
                    schedule, courseListSize);
            Map<String, Room> roomMap = readRoomList(
                    schedule, roomListSize);
            Map<List<Integer>, Period> periodMap = createPeriodListAndDayListAndTimeslotList(
                    schedule, dayListSize, timeslotListSize);
            readCurriculumList(
                    schedule, courseMap, curriculumListSize);
            readUnavailablePeriodPenaltyList(
                    schedule, courseMap, periodMap, unavailablePeriodPenaltyListSize);
            readTeacherName(schedule);
            readTeacherDegree(schedule);
            readTeacherPenaltyList(schedule, periodMap, teacherPenaltyListSize);
            readCourseName(schedule, courseMap);
            readRoomDeps(schedule, courseMap, roomMap);
            //readRoomDeps fonksiyonunda satırı okuduktan sonra hata oluştuğu için
            //bir satır fazla okumuş oluyo ve END. bitişinin üstündeki boş satıra
            //gelmiş oluyor dolayısıyla bi daha boş satır okumasına gerek yok.
            //readEmptyLine();
            readConstantLine("END\\.");
            createLectureList(schedule);

            int possibleForOneLectureSize = schedule.getPeriodList().size() * schedule.getRoomList().size();
            BigInteger possibleSolutionSize = BigInteger.valueOf(possibleForOneLectureSize).pow(
                    schedule.getLectureList().size());
            logger.info("CourseSchedule {} has {} teachers, {} curricula, {} courses, {} lectures,"
                    + " {} periods, {} rooms and {} unavailable period constraints with a search space of {}.",
                    getInputId(),
                    schedule.getTeacherList().size(),
                    schedule.getCurriculumList().size(),
                    schedule.getCourseList().size(),
                    schedule.getLectureList().size(),
                    schedule.getPeriodList().size(),
                    schedule.getRoomList().size(),
                    schedule.getUnavailablePeriodPenaltyList().size(),
                    getFlooredPossibleSolutionSize(possibleSolutionSize));
            for (Teacher t : schedule.getTeacherList()) {
                System.out.println(t.getCode() + "->" + t.getDegree().getId());
            }
            return schedule;
        }

        private void readTeacherName(CourseSchedule schedule) throws IOException {
            readEmptyLine();
            readConstantLine("TEACHER_NAMES:");
            List<Teacher> teachers = schedule.getTeacherList();
            for (int i = 0; i < teachers.size(); i++) {
                String line = bufferedReader.readLine();
                String[] lineTokens = splitBySpacesOrTabs(line);
                String code = lineTokens[0];
                for (Teacher teacher : teachers) {
                    if (teacher.getCode().equals(code)) {
                        if (lineTokens.length == 3) {
                            teacher.setName(lineTokens[1]);
                            teacher.setSurname(lineTokens[2]);
                        } else {
                            teacher.setName(lineTokens[1] + " " + lineTokens[2]);
                            teacher.setSurname(lineTokens[3]);
                        }
                        break;
                    }
                }
            }
        }

        private void readTeacherDegree(CourseSchedule schedule) throws IOException {
            readEmptyLine();
            readConstantLine("TEACHER_DEGREE:");
            List<Teacher> teachers = schedule.getTeacherList();
            for (int i = 0; i < teachers.size(); i++) {
                String line = bufferedReader.readLine();
                String[] lineTokens = splitBySpacesOrTabs(line);
                String code = lineTokens[0];
                Long degreId = Long.parseLong(lineTokens[1]);
                for (Teacher teacher : teachers) {
                    if (teacher.getCode().equals(code)) {
                        teacher.getDegree().setId(degreId);
                    }
                }
            }
        }

        private void readCourseName(CourseSchedule schedule,
                Map<String, Course> courseMap) throws IOException {
            readEmptyLine();
            readConstantLine("COURSE_NAMES:");
            for (int i = 0; i < schedule.getCourseList().size(); i++) {
                String line = bufferedReader.readLine();
                String[] lineTokens = splitBySpacesOrTabs(line);
                String code = lineTokens[0];
                Course course = courseMap.get(code);
                String courseName = "";
                for (int j = 1; j < lineTokens.length; j++) {
                    courseName += lineTokens[j] + " ";
                }
                courseName = courseName.substring(0, courseName.length() - 1);
                course.setName(courseName);
            }
        }

        private void readRoomDeps(CourseSchedule schedule,
                Map<String, Course> courseMap, Map<String, Room> roomMap) throws IOException {
            readEmptyLine();
            readConstantLine("ROOM_DEPS:");
            while (true) {
                try {
                    ArrayList<Room> rooms = new ArrayList<Room>();
                    String line = bufferedReader.readLine();
                    System.out.println("ssss:" + line);
                    String[] lineTokens = splitBySpacesOrTabs(line);
                    String code = lineTokens[0];
                    for (int i = 1; i < lineTokens.length; i++) {
                        Room room = roomMap.get(lineTokens[i]);
                        System.out.println("room:" + room.getLabel());
                        rooms.add(room);

                    }
                    Course course = courseMap.get(code);
                    course.setRoomDeps(rooms);

                } catch (Exception e) {
                    System.out.println("HataBurda:" + e.getMessage());
                    return;
                }
            }
        }

        private Map<String, Course> readCourseListAndTeacherList(
                CourseSchedule schedule, int courseListSize) throws IOException {
            Map<String, Course> courseMap = new HashMap<String, Course>(courseListSize);
            Map<String, Teacher> teacherMap = new HashMap<String, Teacher>();
            List<Course> courseList = new ArrayList<Course>(courseListSize);
            readEmptyLine();
            readConstantLine("COURSES:");
            for (int i = 0; i < courseListSize; i++) {
                Course course = new Course();
                course.setId((long) i);
                // Courses: <CourseID> <Teacher> <# Lectures> <MinWorkingDays> <# Students>
                String line = bufferedReader.readLine();
                String[] lineTokens = splitBySpacesOrTabs(line, 5);
                course.setCode(lineTokens[0]);
                course.setTeacher(findOrCreateTeacher(teacherMap, lineTokens[1]));
                course.setLectureSize(Integer.parseInt(lineTokens[2]));
                course.setMinWorkingDaySize(Integer.parseInt(lineTokens[3]));
                course.setCurriculumList(new ArrayList<Curriculum>());
                course.setStudentSize(Integer.parseInt(lineTokens[4]));
                courseList.add(course);
                courseMap.put(course.getCode(), course);
            }
            schedule.setCourseList(courseList);
            List<Teacher> teacherList = new ArrayList<Teacher>(teacherMap.values());
            schedule.setTeacherList(teacherList);
            return courseMap;
        }

        private Teacher findOrCreateTeacher(Map<String, Teacher> teacherMap, String code) {
            Teacher teacher = teacherMap.get(code);
            if (teacher == null) {
                teacher = new Teacher();
                int id = teacherMap.size();
                teacher.setId((long) id);
                teacher.setCode(code);
                teacherMap.put(code, teacher);
            }
            return teacher;
        }

        private Map<String, Room> readRoomList(CourseSchedule schedule, int roomListSize)
                throws IOException {
            Map<String, Room> roomMap = new HashMap<String, Room>();
            readEmptyLine();
            readConstantLine("ROOMS:");
            List<Room> roomList = new ArrayList<Room>(roomListSize);
            for (int i = 0; i < roomListSize; i++) {
                Room room = new Room();
                room.setId((long) i);
                // Rooms: <RoomID> <Capacity>
                String line = bufferedReader.readLine();
                String[] lineTokens = splitBySpacesOrTabs(line, 2);
                room.setCode(lineTokens[0]);
                room.setCapacity(Integer.parseInt(lineTokens[1]));
                roomList.add(room);
                roomMap.put(room.getCode(), room);
            }
            schedule.setRoomList(roomList);
            return roomMap;
        }

        private Map<List<Integer>, Period> createPeriodListAndDayListAndTimeslotList(
                CourseSchedule schedule, int dayListSize, int timeslotListSize) throws IOException {
            int periodListSize = dayListSize * timeslotListSize;
            Map<List<Integer>, Period> periodMap = new HashMap<List<Integer>, Period>(periodListSize);
            List<Day> dayList = new ArrayList<Day>(dayListSize);
            for (int i = 0; i < dayListSize; i++) {
                Day day = new Day();
                day.setId((long) i);
                day.setDayIndex(i);
                day.setPeriodList(new ArrayList<Period>(timeslotListSize));
                dayList.add(day);
            }
            schedule.setDayList(dayList);
            List<Timeslot> timeslotList = new ArrayList<Timeslot>(timeslotListSize);
            for (int i = 0; i < timeslotListSize; i++) {
                Timeslot timeslot = new Timeslot();
                timeslot.setId((long) i);
                timeslot.setTimeslotIndex(i);
                timeslotList.add(timeslot);
            }
            schedule.setTimeslotList(timeslotList);
            List<Period> periodList = new ArrayList<Period>(periodListSize);
            for (int i = 0; i < dayListSize; i++) {
                Day day = dayList.get(i);
                for (int j = 0; j < timeslotListSize; j++) {
                    Period period = new Period();
                    period.setId((long) (i * timeslotListSize + j));
                    period.setDay(day);
                    period.setTimeslot(timeslotList.get(j));
                    periodList.add(period);
                    periodMap.put(Arrays.asList(i, j), period);
                    day.getPeriodList().add(period);
                }
            }
            schedule.setPeriodList(periodList);
            return periodMap;
        }

        private void readCurriculumList(CourseSchedule schedule,
                Map<String, Course> courseMap, int curriculumListSize) throws IOException {
            readEmptyLine();
            readConstantLine("CURRICULA:");
            List<Curriculum> curriculumList = new ArrayList<Curriculum>(curriculumListSize);
            for (int i = 0; i < curriculumListSize; i++) {
                Curriculum curriculum = new Curriculum();
                curriculum.setId((long) i);
                // Curricula: <CurriculumID> <# Courses> <MemberID> ... <MemberID>
                String line = bufferedReader.readLine();
                String[] lineTokens = splitBySpacesOrTabs(line);
                if (lineTokens.length < 2) {
                    throw new IllegalArgumentException("Read line (" + line
                            + ") is expected to contain at least 2 tokens.");
                }
                curriculum.setCode(lineTokens[0]);
                int coursesInCurriculum = Integer.parseInt(lineTokens[1]);
                if (lineTokens.length != (coursesInCurriculum + 2)) {
                    throw new IllegalArgumentException("Read line (" + line + ") is expected to contain "
                            + (coursesInCurriculum + 2) + " tokens.");
                }
                for (int j = 2; j < lineTokens.length; j++) {
                    Course course = courseMap.get(lineTokens[j]);
                    if (course == null) {
                        throw new IllegalArgumentException("Read line (" + line + ") uses an unexisting course("
                                + lineTokens[j] + ").");
                    }
                    course.getCurriculumList().add(curriculum);
                }
                curriculumList.add(curriculum);
            }
            schedule.setCurriculumList(curriculumList);
        }

        private void readUnavailablePeriodPenaltyList(CourseSchedule schedule, Map<String, Course> courseMap,
                Map<List<Integer>, Period> periodMap, int unavailablePeriodPenaltyListSize)
                throws IOException {
            readEmptyLine();
            readConstantLine("UNAVAILABILITY_CONSTRAINTS:");
            List<UnavailablePeriodPenalty> penaltyList = new ArrayList<UnavailablePeriodPenalty>(
                    unavailablePeriodPenaltyListSize);
            for (int i = 0; i < unavailablePeriodPenaltyListSize; i++) {
                UnavailablePeriodPenalty penalty = new UnavailablePeriodPenalty();
                penalty.setId((long) i);
                // Unavailability_Constraints: <CourseID> <Day> <Day_Period>
                String line = bufferedReader.readLine();
                String[] lineTokens = splitBySpacesOrTabs(line, 3);
                penalty.setCourse(courseMap.get(lineTokens[0]));
                int dayIndex = Integer.parseInt(lineTokens[1]);
                int timeslotIndex = Integer.parseInt(lineTokens[2]);
                Period period = periodMap.get(Arrays.asList(dayIndex, timeslotIndex));
                if (period == null) {
                    throw new IllegalArgumentException("Read line (" + line + ") uses an unexisting period("
                            + dayIndex + " " + timeslotIndex + ").");
                }
                penalty.setPeriod(period);
                penaltyList.add(penalty);
            }
            schedule.setUnavailablePeriodPenaltyList(penaltyList);
        }

        private void readTeacherPenaltyList(CourseSchedule schedule, Map<List<Integer>, Period> periodMap,
                int teacherPenaltyListSize) throws IOException {
            readEmptyLine();
            readConstantLine("TEACHER_UNAVILABILITY_CONSTRAINTS:");
            /**
             * hocanın girdiği dersleri teachersCourseMap'e eklemek için ilk
             * başta dersin hocasının kodunun map içinde bulup bulunmadıgına
             * bakılır bulunmuyorsa yen bir list ile mape eklenir bulunuyorsada
             * o elaman cekilir ve listeye ekleme yapılırç.
             */
            HashMap<String, List<Course>> teachersCourseMap = new HashMap<String, List<Course>>();
            for (Course c : schedule.getCourseList()) {
                String courseTeacherCode = c.getTeacher().getCode();
                if (!teachersCourseMap.containsKey(courseTeacherCode)) {
                    List<Course> teacherCourseList = new ArrayList<Course>();
                    teacherCourseList.add(c);
                    teachersCourseMap.put(courseTeacherCode, teacherCourseList);
                } else {
                    teachersCourseMap.get(courseTeacherCode).add(c);
                }
            }
            for (int i = 0; i < teacherPenaltyListSize; i++) {
                String line = bufferedReader.readLine();
                String lineTokens[] = splitBySpacesOrTabs(line);
                String teacherCode = lineTokens[0];
                int dayIndex = Integer.parseInt(lineTokens[1]);
                int timeslotIndex = Integer.parseInt(lineTokens[2]);
                List<Course> courseList = teachersCourseMap.get(teacherCode);
                for (Course c : courseList) {
                    UnavailablePeriodPenalty penalty = new UnavailablePeriodPenalty();
                    penalty.setCourse(c);
                    Period period = periodMap.get(Arrays.asList(dayIndex, timeslotIndex));
                    if (period == null) {
                        throw new IllegalArgumentException("Read line (" + line + ") uses an unexisting period("
                                + dayIndex + " " + timeslotIndex + ").");
                    }
                    penalty.setPeriod(period);
                    schedule.getUnavailablePeriodPenaltyList().add(penalty);
                }

            }
        }

        private void createLectureList(CourseSchedule schedule) {
            List<Course> courseList = schedule.getCourseList();
            List<Lecture> lectureList = new ArrayList<Lecture>(courseList.size());
            long id = 0L;
            for (Course course : courseList) {
                for (int i = 0; i < course.getLectureSize(); i++) {
                    Lecture lecture = new Lecture();
                    lecture.setId((long) id);
                    id++;
                    lecture.setCourse(course);
                    lecture.setLectureIndexInCourse(i);
                    lecture.setLocked(false);
                    // Notice that we leave the PlanningVariable properties on null
                    lectureList.add(lecture);
                }
            }
            schedule.setLectureList(lectureList);
        }

    }

}
