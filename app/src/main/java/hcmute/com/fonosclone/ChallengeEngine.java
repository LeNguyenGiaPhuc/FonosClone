package hcmute.com.fonosclone;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import hcmute.com.fonosclone.data.ChallengeCompletion;
import hcmute.com.fonosclone.data.FonosDao;
import hcmute.com.fonosclone.data.ListeningHistory;
import hcmute.com.fonosclone.data.UserPoints;

public final class ChallengeEngine {
    private static final int POINTS_ROW_ID = 1;

    private ChallengeEngine() {
    }

    public static ChallengeDashboard evaluate(FonosDao dao) {
        long now = System.currentTimeMillis();
        long dayStart = startOfDay(now);
        long dayEnd = endOfDay(now);
        long weekStart = startOfWeek(now);
        long weekEnd = endOfWeek(now);

        String dayKey = "day_" + formatDate(dayStart);
        String weekKey = "week_" + formatDate(weekStart);

        int dailySeconds = dao.getListenedSecondsBetween(dayStart, dayEnd);
        int weeklySeconds = dao.getListenedSecondsBetween(weekStart, weekEnd);
        int weeklyCompletedBooks = dao.countCompletedBooksBetween(weekStart, weekEnd);
        int weeklyActiveDays = countActiveDays(dao.getListeningHistoryBetween(weekStart, weekEnd));

        List<MissionState> dailyMissions = new ArrayList<>();
        dailyMissions.add(evaluateMission(dao, "daily_5m", dayKey, "Listen or read for 5 minutes", "Easy mission", dailySeconds, 5 * 60, 50));
        dailyMissions.add(evaluateMission(dao, "daily_15m", dayKey, "Listen or read for 15 minutes", "Medium mission", dailySeconds, 15 * 60, 120));
        dailyMissions.add(evaluateMission(dao, "daily_30m", dayKey, "Listen or read for 30 minutes", "Hard mission", dailySeconds, 30 * 60, 250));

        List<MissionState> weeklyMissions = new ArrayList<>();
        weeklyMissions.add(evaluateMission(dao, "weekly_90m", weekKey, "Listen or read for 90 minutes", "Weekly medium mission", weeklySeconds, 90 * 60, 500));
        weeklyMissions.add(evaluateMission(dao, "weekly_finish_1", weekKey, "Finish 1 book", "Reach 90% progress in any book", weeklyCompletedBooks, 1, 700));
        weeklyMissions.add(evaluateMission(dao, "weekly_5_days", weekKey, "Be active for 5 days", "Listen or read on 5 different days", weeklyActiveDays, 5, 1000));

        int totalPoints = dao.getTotalAwardedPoints();
        UserPoints points = dao.getUserPoints();
        if (points == null || points.totalPoints != totalPoints) {
            dao.upsertUserPoints(new UserPoints(POINTS_ROW_ID, totalPoints));
        }

        return new ChallengeDashboard(totalPoints, dailySeconds, weeklySeconds, dailyMissions, weeklyMissions);
    }

    private static MissionState evaluateMission(
            FonosDao dao,
            String missionId,
            String periodKey,
            String title,
            String subtitle,
            int progress,
            int target,
            int points
    ) {
        boolean alreadyCompleted = dao.isMissionCompleted(missionId, periodKey) > 0;
        boolean targetReached = progress >= target;
        if (targetReached && !alreadyCompleted) {
            dao.insertChallengeCompletion(new ChallengeCompletion(
                    missionId,
                    periodKey,
                    points,
                    System.currentTimeMillis()
            ));
            alreadyCompleted = true;
        }

        return new MissionState(
                title,
                subtitle,
                Math.min(progress, target),
                target,
                points,
                alreadyCompleted
        );
    }

    private static int countActiveDays(List<ListeningHistory> history) {
        Set<String> days = new HashSet<>();
        for (ListeningHistory item : history) {
            if (item.listenedSeconds > 0) {
                days.add(formatDate(item.listenedAt));
            }
        }
        return days.size();
    }

    private static long startOfDay(long timeMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private static long endOfDay(long timeMillis) {
        return startOfDay(timeMillis) + 24L * 60L * 60L * 1000L - 1L;
    }

    private static long startOfWeek(long timeMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private static long endOfWeek(long timeMillis) {
        return startOfWeek(timeMillis) + 7L * 24L * 60L * 60L * 1000L - 1L;
    }

    private static String formatDate(long timeMillis) {
        return new SimpleDateFormat("yyyyMMdd", Locale.US).format(timeMillis);
    }

    public static final class ChallengeDashboard {
        public final int totalPoints;
        public final int dailySeconds;
        public final int weeklySeconds;
        public final List<MissionState> dailyMissions;
        public final List<MissionState> weeklyMissions;

        ChallengeDashboard(
                int totalPoints,
                int dailySeconds,
                int weeklySeconds,
                List<MissionState> dailyMissions,
                List<MissionState> weeklyMissions
        ) {
            this.totalPoints = totalPoints;
            this.dailySeconds = dailySeconds;
            this.weeklySeconds = weeklySeconds;
            this.dailyMissions = dailyMissions;
            this.weeklyMissions = weeklyMissions;
        }
    }

    public static final class MissionState {
        public final String title;
        public final String subtitle;
        public final int progress;
        public final int target;
        public final int points;
        public final boolean completed;

        MissionState(String title, String subtitle, int progress, int target, int points, boolean completed) {
            this.title = title;
            this.subtitle = subtitle;
            this.progress = progress;
            this.target = target;
            this.points = points;
            this.completed = completed;
        }
    }
}
