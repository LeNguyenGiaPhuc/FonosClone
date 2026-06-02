const fs = require("fs");
const path = require("path");
const admin = require("firebase-admin");

function getArg(name, fallback = "") {
  const prefix = `--${name}=`;
  const inlineArg = process.argv.find((arg) => arg.startsWith(prefix));
  if (inlineArg) return inlineArg.slice(prefix.length);

  const index = process.argv.indexOf(`--${name}`);
  if (index >= 0 && process.argv[index + 1]) return process.argv[index + 1];

  return fallback;
}

function loadJson(filePath) {
  const absolutePath = path.resolve(process.cwd(), filePath);
  return JSON.parse(fs.readFileSync(absolutePath, "utf8"));
}

function requireFile(filePath, label) {
  if (!filePath) {
    throw new Error(`Missing ${label}. Use --${label}=path/to/file.json`);
  }

  const absolutePath = path.resolve(process.cwd(), filePath);
  if (!fs.existsSync(absolutePath)) {
    throw new Error(`${label} not found: ${absolutePath}`);
  }

  return absolutePath;
}

async function commitInBatches(items, createWrite) {
  const db = admin.firestore();
  let batch = db.batch();
  let count = 0;
  let committed = 0;

  for (const item of items) {
    createWrite(batch, item);
    count++;

    if (count === 450) {
      await batch.commit();
      committed += count;
      batch = db.batch();
      count = 0;
    }
  }

  if (count > 0) {
    await batch.commit();
    committed += count;
  }

  return committed;
}

async function main() {
  const serviceAccountPath = requireFile(
    getArg("serviceAccount", process.env.GOOGLE_APPLICATION_CREDENTIALS || ""),
    "serviceAccount"
  );
  const booksPath = getArg("books", "books.seed.json");
  const podCoursesPath = getArg("podcourses", "pod_courses.seed.json");

  const serviceAccount = loadJson(serviceAccountPath);
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });

  const db = admin.firestore();
  const books = loadJson(booksPath);
  const podCourses = loadJson(podCoursesPath);

  const bookCount = await commitInBatches(books, (batch, book) => {
    const id = Number(book.id);
    if (!Number.isInteger(id) || id <= 0) {
      throw new Error(`Invalid book id: ${JSON.stringify(book)}`);
    }

    batch.set(db.collection("books").doc(`book_${id}`), {
      id,
      title: book.title || "",
      author: book.author || "",
      type: book.type || "AUDIOBOOK",
      category: book.category || "",
      coverImage: book.coverImage || "book",
      audioResName: book.audioResName || "demo_audio",
      audioUrl: book.audioUrl || "",
      audioStoragePath: book.audioStoragePath || "",
      isFavorite: Boolean(book.isFavorite)
    });
  });

  const podCourseCount = await commitInBatches(podCourses, (batch, course) => {
    const id = Number(course.id);
    if (!Number.isInteger(id) || id <= 0) {
      throw new Error(`Invalid pod course id: ${JSON.stringify(course)}`);
    }

    batch.set(db.collection("pod_courses").doc(`course_${id}`), {
      id,
      title: course.title || "",
      teacher: course.teacher || "",
      category: course.category || "",
      coverColor: course.coverColor || "#1E8080",
      rating: Number(course.rating || 4.5)
    });
  });

  console.log(`Imported ${bookCount} books.`);
  console.log(`Imported ${podCourseCount} pod courses.`);
  console.log("Done.");
}

main().catch((error) => {
  console.error(error.message);
  process.exit(1);
});
