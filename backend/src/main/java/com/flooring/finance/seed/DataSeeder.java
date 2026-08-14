package com.flooring.finance.seed;

import com.flooring.finance.common.EntryCategory;
import com.flooring.finance.entity.Job;
import com.flooring.finance.entity.JobEntry;
import com.flooring.finance.entity.User;
import com.flooring.finance.repository.JobRepository;
import com.flooring.finance.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Random;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Development-only seed data: five hand-written flooring jobs (used
 * throughout the docs/screenshots) plus a larger batch of programmatically
 * generated ones spread across the last ~20 months, so the job list reads
 * like a real, ongoing business rather than a handful of demo rows. Runs
 * only under the "dev" profile and only if the database is empty.
 * Generation is seeded ({@link #RANDOM}) so the dataset is identical on
 * every fresh run.
 * <p>
 * <b>Seed login (development only - never used in production):</b>
 * username {@code owner}, password {@code ChangeMe123!}.
 */
@Component
@Profile("dev")
public class DataSeeder implements CommandLineRunner {

    private static final int GENERATED_JOB_COUNT = 65;
    private static final Random RANDOM = new Random(42);

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, JobRepository jobRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // already seeded
        }

        userRepository.save(User.builder()
                .username("owner")
                .email("owner@flooringfinance.my")
                .fullName("Business Owner")
                .passwordHash(passwordEncoder.encode("ChangeMe123!"))
                .build());

        seedTamanMelawati();
        seedShahAlamOffice();
        seedKajangHouse();
        seedSerembanShop();
        seedJohorBahruProject();
        seedGeneratedJobs(GENERATED_JOB_COUNT);
    }

    // ------------------------------------------------------------------
    // Five hand-written jobs - referenced by the README and screenshots.
    // Totals match what earlier versions of this app produced.
    // ------------------------------------------------------------------

    private void seedTamanMelawati() {
        Job job = Job.builder()
                .name("Taman Melawati House")
                .customerName("Ahmad")
                .location("Taman Melawati, Kuala Lumpur")
                .jobDate(LocalDate.of(2026, 5, 15))
                .notes("500 sq ft SPC vinyl plank, living and dining area.")
                .build();
        addEntry(job, EntryCategory.INCOME, "Full payment", "15000.00");
        addEntry(job, EntryCategory.MATERIALS, "SPC vinyl plank + adhesive", "6500.00");
        addEntry(job, EntryCategory.WORKER, "3 days at RM466.67/day", "1400.00");
        addEntry(job, EntryCategory.DELIVERY, "Lorry hire for materials", "600.00");
        addEntry(job, EntryCategory.OTHER, "Misc", "340.00");
        jobRepository.save(job);
    }

    private void seedShahAlamOffice() {
        Job job = Job.builder()
                .name("Shah Alam Office")
                .customerName("Tan Wei Ming")
                .location("Seksyen 15, Shah Alam")
                .jobDate(LocalDate.of(2026, 6, 10))
                .notes("Office renovation - laminate flooring throughout.")
                .build();
        addEntry(job, EntryCategory.INCOME, "Full payment", "20000.00");
        addEntry(job, EntryCategory.MATERIALS, "Laminate flooring", "9000.00");
        addEntry(job, EntryCategory.WORKER, "4 days at RM800/day", "3200.00");
        addEntry(job, EntryCategory.DELIVERY, "Lorry hire for materials", "1100.00");
        addEntry(job, EntryCategory.OTHER, "Misc", "700.00");
        jobRepository.save(job);
    }

    private void seedKajangHouse() {
        Job job = Job.builder()
                .name("Kajang House")
                .customerName("Siti Rahman")
                .location("Bandar Kajang, Selangor")
                .jobDate(LocalDate.of(2026, 7, 5))
                .notes("Vinyl flooring for 3-bedroom house.")
                .build();
        addEntry(job, EntryCategory.INCOME, "Full payment", "13500.00");
        addEntry(job, EntryCategory.MATERIALS, "Vinyl flooring", "5500.00");
        addEntry(job, EntryCategory.WORKER, "3 days at RM666.67/day", "2000.00");
        addEntry(job, EntryCategory.DELIVERY, "Lorry hire for materials", "500.00");
        addEntry(job, EntryCategory.OTHER, "Misc", "360.00");
        jobRepository.save(job);
    }

    private void seedSerembanShop() {
        Job job = Job.builder()
                .name("Seremban Shop")
                .customerName("Lim Kok Wei")
                .location("Seremban 2, Negeri Sembilan")
                .jobDate(LocalDate.of(2026, 8, 1))
                .notes("Retail shop lot - homogeneous tile flooring. Deposit collected, work ongoing.")
                .build();
        addEntry(job, EntryCategory.INCOME, "Deposit", "18000.00");
        addEntry(job, EntryCategory.MATERIALS, "Homogeneous tiles + grout", "7000.00");
        addEntry(job, EntryCategory.WORKER, "3 days at RM600/day", "1800.00");
        addEntry(job, EntryCategory.DELIVERY, "Lorry hire for materials", "520.00");
        addEntry(job, EntryCategory.OTHER, "Misc", "360.00");
        jobRepository.save(job);
    }

    private void seedJohorBahruProject() {
        Job job = Job.builder()
                .name("Johor Bahru Project")
                .customerName("Rajesh Kumar")
                .location("Taman Molek, Johor Bahru")
                .jobDate(LocalDate.of(2026, 8, 8))
                .notes("Commercial unit - parquet flooring for reception and offices.")
                .build();
        addEntry(job, EntryCategory.INCOME, "Full payment", "22000.00");
        addEntry(job, EntryCategory.MATERIALS, "Parquet flooring", "10000.00");
        addEntry(job, EntryCategory.WORKER, "3 days at RM833.33/day", "2500.00");
        addEntry(job, EntryCategory.DELIVERY, "Lorry hire for materials", "800.00");
        addEntry(job, EntryCategory.OTHER, "Misc", "500.00");
        jobRepository.save(job);
    }

    // ------------------------------------------------------------------
    // Bulk-generated jobs - same shape of data, spread across ~20 months.
    // ------------------------------------------------------------------

    private static final String[] CUSTOMER_NAMES = {
            "Nurul Ain", "Wong Mei Ling", "Muthu Samy", "Chong Ah Seng", "Fatimah Zahra",
            "Hafiz Rahman", "Cheah Boon Huat", "Aisyah Ismail", "Kumar Selvam", "Ng Wei Jian",
            "Zainab Hussain", "Teoh Beng Hock", "Priya Devi", "Amirul Haziq", "Lee Chong Wei",
            "Balasubramaniam", "Yeoh Siew Fong", "Ismail Kassim", "Chan Kar Weng", "Farah Diyana",
            "Ravindran Nair", "Ooi Poh Ling", "Azman Yusof", "Tan Sri Lim", "Suresh Kumar",
            "Halimah Yacob", "Goh Chin Huat", "Nabila Sharif", "Vijayakumar", "Foo Yee Ching",
            "Rosnah Ibrahim", "Lau Tuck Meng", "Shanmugam Pillai", "Aina Syafiqah", "Chua Beng Guan",
            "Maimunah Salleh", "Devan Raj", "Loh Kok Seng", "Nurhaliza Othman", "Chin Wai Keong",
            "Anitha Rajoo", "Hasnah Daud", "Koh Boon Lay", "Faridah Merican", "Sivakumar",
        };

    // Peninsular Malaysia towns, matching this business's coverage.
    private static final String[] LOCATIONS = {
            "Skudai", "Batu Pahat", "Muar", "Kulai", "Taman Molek, Johor Bahru",
            "Alor Setar", "Sungai Petani", "Kulim", "Kota Bharu", "Pasir Mas",
            "Melaka City", "Ayer Keroh", "Alor Gajah", "Seremban 2, Negeri Sembilan", "Nilai", "Port Dickson",
            "Kuantan", "Temerloh", "Bentong", "George Town", "Bayan Lepas", "Butterworth", "Bukit Mertajam",
            "Ipoh", "Taiping", "Sitiawan", "Kangar", "Shah Alam", "Petaling Jaya", "Subang Jaya",
            "Klang", "Kajang", "Cheras", "Puchong", "Rawang", "Kuala Terengganu", "Dungun",
            "Bukit Bintang", "Ampang", "Setapak", "Taman Melawati, Kuala Lumpur", "Bangsar", "Cheras KL", "Putrajaya",
    };

    private static final String[] JOB_KINDS = {"House", "Office", "Shop", "Condo", "Apartment", "Renovation", "Terrace House", "Bungalow"};

    /** Flooring type -> notes fragment. */
    private static final String[][] FLOORING_TYPES = {
            {"SPC Vinyl Plank", "SPC vinyl plank flooring"},
            {"Laminate Flooring", "laminate flooring"},
            {"Homogeneous Tiles", "homogeneous tile flooring"},
            {"Solid Parquet", "solid parquet flooring"},
            {"Engineered Wood Flooring", "engineered wood flooring"},
            {"Vinyl Sheet", "vinyl sheet flooring"},
            {"Ceramic Tiles", "ceramic tile flooring"},
            {"Carpet Tiles", "carpet tile flooring"},
    };

    private static final String[] AREAS = {
            "living and dining area", "whole unit", "bedrooms and hallway",
            "reception and offices", "retail floor", "kitchen and utility area", "common areas",
    };

    private void seedGeneratedJobs(int count) {
        LocalDate today = LocalDate.now();

        for (int i = 0; i < count; i++) {
            String location = LOCATIONS[RANDOM.nextInt(LOCATIONS.length)];
            String kind = JOB_KINDS[RANDOM.nextInt(JOB_KINDS.length)];
            String customer = CUSTOMER_NAMES[RANDOM.nextInt(CUSTOMER_NAMES.length)];
            String[] flooring = FLOORING_TYPES[RANDOM.nextInt(FLOORING_TYPES.length)];
            String area = AREAS[RANDOM.nextInt(AREAS.length)];

            int daysAgo = RANDOM.nextInt(610); // spread across ~20 months
            LocalDate jobDate = today.minusDays(daysAgo);

            BigDecimal collection = randomRoundAmount(6000, 45000);
            int sqft = 300 + RANDOM.nextInt(1400);

            BigDecimal materialsCost = pct(collection, 0.32, 0.45);

            int workerDays = 1 + RANDOM.nextInt(6);
            BigDecimal workerRatePerDay = randomRoundAmount(80, 250);
            BigDecimal workerCost = workerRatePerDay.multiply(BigDecimal.valueOf(workerDays)).setScale(2, RoundingMode.HALF_UP);

            // Covers what used to be delivery + other + worker food - split
            // between a Delivery entry and a smaller Other catch-all.
            BigDecimal miscCosts = pct(collection, 0.03, 0.08);
            BigDecimal deliveryShare = pct(miscCosts, 0.4, 0.75);
            BigDecimal deliveryCost = deliveryShare.setScale(2, RoundingMode.HALF_UP);
            BigDecimal otherCosts = miscCosts.subtract(deliveryCost);

            Job job = Job.builder()
                    .name(location + " " + kind)
                    .customerName(customer)
                    .location(location)
                    .jobDate(jobDate)
                    .notes(sqft + " sq ft " + flooring[1] + ", " + area + ".")
                    .build();
            addEntry(job, EntryCategory.INCOME, "Full payment", collection);
            addEntry(job, EntryCategory.MATERIALS, flooring[0], materialsCost);
            addEntry(job, EntryCategory.WORKER, workerDays + " days at " + myr(workerRatePerDay) + "/day", workerCost);
            addEntry(job, EntryCategory.DELIVERY, "Lorry hire for materials", deliveryCost);
            addEntry(job, EntryCategory.OTHER, "Misc", otherCosts);

            jobRepository.save(job);
        }
    }

    /** A random amount, rounded to the nearest 100, between min and max (inclusive-ish). */
    private BigDecimal randomRoundAmount(int min, int max) {
        int raw = min + RANDOM.nextInt(max - min);
        int rounded = Math.round(raw / 100f) * 100;
        return new BigDecimal(rounded).setScale(2, RoundingMode.HALF_UP);
    }

    /** {@code base * a random percentage in [minPct, maxPct]}, rounded to 2dp. */
    private BigDecimal pct(BigDecimal base, double minPct, double maxPct) {
        double p = minPct + RANDOM.nextDouble() * (maxPct - minPct);
        return base.multiply(BigDecimal.valueOf(p)).setScale(2, RoundingMode.HALF_UP);
    }

    private void addEntry(Job job, EntryCategory category, String description, String amount) {
        addEntry(job, category, description, new BigDecimal(amount));
    }

    private void addEntry(Job job, EntryCategory category, String description, BigDecimal amount) {
        job.getEntries().add(JobEntry.builder()
                .job(job)
                .category(category)
                .description(description)
                .amount(amount)
                .build());
    }

    private String myr(BigDecimal amount) {
        return "RM" + amount.setScale(0, RoundingMode.HALF_UP);
    }
}
