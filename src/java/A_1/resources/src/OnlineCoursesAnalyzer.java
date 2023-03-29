package A_1.resources.src;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Copy;

public class OnlineCoursesAnalyzer {
    List<Course> courses = new ArrayList<>();

    public OnlineCoursesAnalyzer(String datasetPath) {
        BufferedReader br = null;
        String line;
        try {
            br = new BufferedReader(new FileReader(datasetPath, StandardCharsets.UTF_8));
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] info = line.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
                Course course = new Course(info[0], info[1], new Date(info[2]), info[3], info[4], info[5],
                        Integer.parseInt(info[6]), Integer.parseInt(info[7]), Integer.parseInt(info[8]),
                        Integer.parseInt(info[9]), Integer.parseInt(info[10]), Double.parseDouble(info[11]),
                        Double.parseDouble(info[12]), Double.parseDouble(info[13]), Double.parseDouble(info[14]),
                        Double.parseDouble(info[15]), Double.parseDouble(info[16]), Double.parseDouble(info[17]),
                        Double.parseDouble(info[18]), Double.parseDouble(info[19]), Double.parseDouble(info[20]),
                        Double.parseDouble(info[21]), Double.parseDouble(info[22]));
                courses.add(course);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    //1
    public Map<String, Integer> getPtcpCountByInst() {
            Map<String, Integer> countByInst = new TreeMap<>();
            for (Course course : courses) {
                String institution = course.institution;
                int participants = course.participants;
                countByInst.put(institution, countByInst.getOrDefault(institution, 0) + participants);
            }
            return countByInst;
    }

    //2

    public Map<String, Integer> getPtcpCountByInstAndSubject() {
        Map<String, Integer> countByInstAndSubject = new TreeMap<>(Collections.reverseOrder());
        for (Course course : courses) {
            String institution = course.institution;
            String subject = course.subject;
            String key = institution + "-" + subject;
            int participants = course.participants;
            countByInstAndSubject.put(key, countByInstAndSubject.getOrDefault(key, 0) + participants);
        }
        return countByInstAndSubject;
    }


    //3
    public Map<String, List<List<String>>> getCourseListOfInstructor() {
        Map<String, List<List<String>>> courseListByInstructor = new TreeMap<>();
        for (Course course : courses) {
            String[] instructors = course.instructors.split(", ");
            for (String instructor : instructors) {
                List<List<String>> courseLists = courseListByInstructor.getOrDefault(instructor, new ArrayList<>(2));
                if (courseLists.size() == 0) {
                    courseLists.add(new ArrayList<>());
                    courseLists.add(new ArrayList<>());
                }
                if (instructors.length == 1) {
                    courseLists.get(0).add(course.title);
                } else {
                    courseLists.get(1).add(course.title);
                }
                courseListByInstructor.put(instructor, courseLists);
            }
        }
        return courseListByInstructor;
    }

    //4
    public List<String> getCourses(int topK, String by) {
        Map<String, Course> courseMap = new HashMap<>();
        for (Course course : courses) {
            courseMap.put(course.title, course);
        }
        List<Course> sortedCourses = new ArrayList<>(courseMap.values());
        if (by.equals("hours")) {
            sortedCourses.sort(Comparator.comparing(Course::getTotalHours).reversed().thenComparing(Course::getTitle));
        } else if (by.equals("participants")) {
            sortedCourses.sort(Comparator.comparing(Course::getParticipants).reversed().thenComparing(Course::getTitle));
        }
        List<String> result = new ArrayList<>();
        for (int i = 0; i < Math.min(topK, sortedCourses.size()); i++) {
            result.add(sortedCourses.get(i).title);
        }
        return result;
    }

    //5
    public List<String> searchCourses(String courseSubject, double percentAudited, double totalCourseHours) {
        List<String> result = new ArrayList<>();
        for (Course course : courses) {
            if (course.subject.toLowerCase().contains(courseSubject.toLowerCase())
                && course.percentAudited >= percentAudited
                && course.totalHours <= totalCourseHours) {
                result.add(course.title);
            }
        }
        Collections.sort(result);
        return result;
    }



    //6
    public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {
        Map<String, List<Course>> courseMap = new HashMap<>();
        for (Course course : courses) {
            if (!courseMap.containsKey(course.number)) {
                courseMap.put(course.number, new ArrayList<>());
            }
            courseMap.get(course.number).add(course);
        }
        List<Course> courseList = new ArrayList<>();
        for (List<Course> courseGroup : courseMap.values()) {
            int n = courseGroup.size();
            int totalMedianAge = 0;
            int totalMale = 0;
            int totalBachelorOrHigher = 0;
            for (Course course : courseGroup) {
                totalMedianAge += course.medianAge;
                totalMale += course.percentMale;
                totalBachelorOrHigher += course.percentDegree;
            }
            int averageMedianAge = totalMedianAge / n;
            int averageMale = totalMale / n;
            int averageBachelorOrHigher = totalBachelorOrHigher / n;
            Course latestCourse = courseGroup.stream().max(Comparator.comparing(Course::getLaunchDate)).get();
            courseList.add(new Course(courseGroup.get(0).number, latestCourse.title, averageMedianAge, averageMale, averageBachelorOrHigher, latestCourse.launchDate));
        }
        List<Course> sortedCourses = new ArrayList<>();
        for (Course course : courseList) {
            double similarityValue = (age - course.medianAge) * (age - course.medianAge) + (gender * 100 - course.percentMale) * (gender * 100 - course.percentMale) + (isBachelorOrHigher * 100 - course.percentDegree) * (isBachelorOrHigher * 100 - course.percentDegree);
            course.setSimilarityValue(similarityValue);
            sortedCourses.add(course);
        }
        sortedCourses.sort(Comparator.comparing(Course::getSimilarityValue).thenComparing(Course::getTitle));
        List<String> result = new ArrayList<>();
        for (int i = 0; i < Math.min(10, sortedCourses.size()); i++) {
            result.add(sortedCourses.get(i).title);
        }
        return result;
    }

    public ArrayList<Course> getCourses() {
        return (ArrayList<Course>) courses;
    }
}

class Course {



    public String getInstitution() {
        return institution;
    }

    public String getNumber() {
        return number;
    }

    public Date getLaunchDate() {
        return launchDate;
    }

    public String getTitle() {
        return title;
    }

    public String getInstructors() {
        return instructors;
    }

    public String getSubject() {
        return subject;
    }

    public int getYear() {
        return year;
    }

    public int getHonorCode() {
        return honorCode;
    }

    public int getParticipants() {
        return participants;
    }

    public int getAudited() {
        return audited;
    }

    public int getCertified() {
        return certified;
    }

    public double getPercentAudited() {
        return percentAudited;
    }

    public double getPercentCertified() {
        return percentCertified;
    }

    public double getPercentCertified50() {
        return percentCertified50;
    }

    public double getPercentVideo() {
        return percentVideo;
    }

    public double getPercentForum() {
        return percentForum;
    }

    public double getGradeHigherZero() {
        return gradeHigherZero;
    }

    public double getTotalHours() {
        return totalHours;
    }

    public double getMedianHoursCertification() {
        return medianHoursCertification;
    }

    public double getMedianAge() {
        return medianAge;
    }

    public double getPercentMale() {
        return percentMale;
    }

    public double getPercentFemale() {
        return percentFemale;
    }

    public double getPercentDegree() {
        return percentDegree;
    }

    String institution;
    String number;
    Date launchDate;
    String title;
    String instructors;
    String subject;
    int year;
    int honorCode;
    int participants;
    int audited;
    int certified;
    double percentAudited;
    double percentCertified;
    double percentCertified50;
    double percentVideo;
    double percentForum;
    double gradeHigherZero;
    double totalHours;
    double medianHoursCertification;
    double medianAge;
    double percentMale;
    double percentFemale;
    double percentDegree;

    public double getSimilarityValue() {
        return SimilarityValue;
    }

    public void setSimilarityValue(double similarityValue) {
        SimilarityValue = similarityValue;
    }

    double SimilarityValue;

    public Course(String institution, String title, int medianAge, int percentMale, int percentDegree, Date launchDate) {
        this.institution = institution;
        this.title = title;
        this.medianAge = medianAge;
        this.percentMale = percentMale;
        this.percentDegree = percentDegree;
        this.launchDate = launchDate;
    }


    public Course(String institution, String number, Date launchDate,
                  String title, String instructors, String subject,
                  int year, int honorCode, int participants,
                  int audited, int certified, double percentAudited,
                  double percentCertified, double percentCertified50,
                  double percentVideo, double percentForum, double gradeHigherZero,
                  double totalHours, double medianHoursCertification,
                  double medianAge, double percentMale, double percentFemale,
                  double percentDegree) {
        this.institution = institution;
        this.number = number;
        this.launchDate = launchDate;
        if (title.startsWith("\"")) title = title.substring(1);
        if (title.endsWith("\"")) title = title.substring(0, title.length() - 1);
        this.title = title;
        if (instructors.startsWith("\"")) instructors = instructors.substring(1);
        if (instructors.endsWith("\"")) instructors = instructors.substring(0, instructors.length() - 1);
        this.instructors = instructors;
        if (subject.startsWith("\"")) subject = subject.substring(1);
        if (subject.endsWith("\"")) subject = subject.substring(0, subject.length() - 1);
        this.subject = subject;
        this.year = year;
        this.honorCode = honorCode;
        this.participants = participants;
        this.audited = audited;
        this.certified = certified;
        this.percentAudited = percentAudited;
        this.percentCertified = percentCertified;
        this.percentCertified50 = percentCertified50;
        this.percentVideo = percentVideo;
        this.percentForum = percentForum;
        this.gradeHigherZero = gradeHigherZero;
        this.totalHours = totalHours;
        this.medianHoursCertification = medianHoursCertification;
        this.medianAge = medianAge;
        this.percentMale = percentMale;
        this.percentFemale = percentFemale;
        this.percentDegree = percentDegree;
    }


}












