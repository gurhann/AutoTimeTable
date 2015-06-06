/*
 * Copyright 2015 JBoss by Red Hat.
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
package org.optaplanner.curriculumcourse.special_content;

import java.io.IOException;
import javax.persistence.EntityManager;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.optaplanner.curriculumcourse.Message;
import org.optaplanner.curriculumcourse.dao.CourseDao;
import org.optaplanner.curriculumcourse.dao.TeacherDao;
import org.optaplanner.examples.curriculumcourse.domain.Course;
import org.optaplanner.examples.curriculumcourse.domain.Teacher;

/**
 *
 * @author gurhan
 */
@WebServlet("/curriculumcourse/AssignTheLesson")

public class AssignTheLesson extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        Message message = new Message();
        EntityManager em = (EntityManager) req.getServletContext().getAttribute("entityManager");
        CourseDao cDao = new CourseDao(em);
        TeacherDao tDao = new TeacherDao(em);
        Long teacherId = Long.parseLong(req.getParameter("teacherId"));
        Long courseId = Long.parseLong(req.getParameter("courseId"));
        try {
            Teacher teacher = tDao.find(Teacher.class, teacherId);
            Course course = cDao.find(Course.class, courseId);
            course.setTeacher(teacher);
            cDao.update(course);
            message.setResult(true);
            message.setContent("Değişiklikler başarıyla kaydedildi");
        } catch (Exception e) {
            e.printStackTrace();
            message.setResult(false);
            message.setContent("Bir sorun oluştu.");
        }
        RequestDispatcher rd = req.getRequestDispatcher("TeacherCoursesEdit");
        rd.forward(req, resp);
    }

}
