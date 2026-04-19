var foundCount = INITIAL_FOUND;
var score = INITIAL_SCORE;
var secondsUsed = 0;
var timerInterval = null;
var gameFinished = false; // ✅ FLAG — bloque tout quand true

window.onload = function() {
    document.getElementById("foundCount").textContent = foundCount;
    document.getElementById("scoreDisplay").textContent = score;
    if (TIME_LIMIT > 0) startTimer();
    setInterval(autoSave, 30000);
};

function autoSave() {
    if (gameFinished) return; // ✅ pas de sauvegarde si fini
    var levelVal = document.getElementById("hiddenLevel").value;
    fetch("/save", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: "level=" + levelVal +
              "&differencesFound=" + (foundCount || 0) +
              "&currentScore=" + (score || 0) +
              "&timeRemaining=" + (document.getElementById("hiddenTime").value || 0)
    });
}

function startTimer() {
    var timeLeft = INITIAL_TIME > 0 ? INITIAL_TIME : TIME_LIMIT;
    timerInterval = setInterval(function() {
        if (gameFinished) { clearInterval(timerInterval); return; }
        timeLeft--;
        secondsUsed++;
        var timerEl = document.getElementById("timer");
        if (timerEl) timerEl.textContent = timeLeft;
        document.getElementById("hiddenTime").value = timeLeft;
		if (timeLeft <= 0) {
		    clearInterval(timerInterval);
		    gameFinished = true;
		    finishGame();
		}
    }, 1000);
}

var differences = {
    1: [
        { x: 51.8, y: 20.4, r: 6 }, { x: 90.4, y: 10.8, r: 6 },
        { x: 84.1, y: 79.3, r: 6 }, { x: 38.8, y: 89.8, r: 6 },
        { x: 28.1, y: 98.0, r: 6 }, { x: 1.8,  y: 77.5, r: 6 },
        { x: 7.1,  y: 36.8, r: 6 }, { x: 68.4, y: 39.6, r: 6 },
        { x: 53.1, y: 61.5, r: 6 }, { x: 96.4, y: 93.9, r: 6 }
    ],
    2: [
        { x: 84.4, y: 58.8, r: 6 }, { x: 95.4, y: 50.0, r: 6 },
        { x: 74.4, y: 41.3, r: 6 }, { x: 67.4, y: 31.4, r: 6 },
        { x: 22.2, y: 10.6, r: 6 }, { x: 15.8, y: 16.6, r: 6 },
        { x: 8.2,  y: 75.3, r: 6 }, { x: 40.7, y: 89.5, r: 6 },
        { x: 34.6, y: 66.5, r: 6 }, { x: 90.5, y: 78.0, r: 6 }
    ],
    3: [
        { x: 62.2, y: 22.8, r: 6 }, { x: 44.4, y: 42.4, r: 6 },
        { x: 85.0, y: 75.9, r: 6 }, { x: 72.6, y: 41.1, r: 6 },
        { x: 77.1, y: 14.3, r: 6 }, { x: 53.3, y: 20.1, r: 6 },
        { x: 22.8, y: 79.9, r: 6 }, { x: 16.5, y: 90.6, r: 6 },
        { x: 9.8,  y: 22.8, r: 6 }, { x: 38.0, y: 96.0, r: 6 }
    ],
    4: [
        { x: 40.1, y: 94.3, r: 6 }, { x: 66.1, y: 87.6, r: 6 },
        { x: 63.7, y: 24.9, r: 6 }, { x: 25.5, y: 4.2,  r: 6 },
        { x: 42.5, y: 11.5, r: 6 }, { x: 38.8, y: 65.2, r: 6 },
        { x: 3.6,  y: 42.3, r: 6 }, { x: 8.8,  y: 14.9, r: 6 },
        { x: 84.7, y: 51.8, r: 6 }, { x: 98.3, y: 14.9, r: 6 }
    ],
    5: [
        { x: 7.1,  y: 24.6, r: 6 }, { x: 29.1, y: 2.8,  r: 6 },
        { x: 87.4, y: 30.3, r: 6 }, { x: 94.3, y: 94.4, r: 6 },
        { x: 82.9, y: 75.4, r: 6 }, { x: 29.1, y: 62.7, r: 6 },
        { x: 42.0, y: 79.6, r: 6 }, { x: 49.9, y: 95.8, r: 6 },
        { x: 8.6,  y: 88.7, r: 6 }, { x: 71.1, y: 85.2, r: 6 }
    ]
};

var foundIndexes = [];

function handleClick(event, imgElement) {
    // ✅ Bloque si jeu terminé
    if (gameFinished) return;

    var rect = imgElement.getBoundingClientRect();
    var clickX = event.clientX - rect.left;
    var clickY = event.clientY - rect.top;
    var clickXPercent = (clickX / rect.width) * 100;
    var clickYPercent = (clickY / rect.height) * 100;

    var levelKey = parseInt(document.getElementById("hiddenLevel").value);
    var levelDiffs = differences[levelKey];

    for (var i = 0; i < levelDiffs.length; i++) {
        if (foundIndexes.includes(i)) continue;

        var diff = levelDiffs[i];
        var distance = Math.sqrt(
            Math.pow(clickXPercent - diff.x, 2) +
            Math.pow(clickYPercent - diff.y, 2)
        );

        if (distance <= diff.r) {
            foundIndexes.push(i);
            foundCount++;
            score += 200;

            showCircle(clickX, clickY, imgElement, "correct");

            var imgOriginal = document.getElementById("img-original");
            var origX = (clickXPercent / 100) * imgOriginal.offsetWidth;
            var origY = (clickYPercent / 100) * imgOriginal.offsetHeight;
            showCircle(origX, origY, imgOriginal, "correct");

            document.getElementById("foundCount").textContent = foundCount;
            document.getElementById("scoreDisplay").textContent = score;
            document.getElementById("hiddenFound").value = foundCount;
            document.getElementById("hiddenScore").value = score;
            document.getElementById("hiddenFound2").value = foundCount;

			if (foundCount >= TOTAL_DIFFERENCES) {
			    clearInterval(timerInterval);
			    gameFinished = true;
			    setTimeout(function() {
			        finishGame();
			    }, 300); // ✅ Attend 600ms pour voir le dernier cercle vert
			}
			
            return;
        }
    }

    showCircle(clickX, clickY, imgElement, "wrong");
    score = Math.max(0, score - 50);
    document.getElementById("scoreDisplay").textContent = score;
    document.getElementById("hiddenScore").value = score;
}

function showCircle(x, y, img, type) {
    var marker = document.createElement("div");
    marker.className = type === "correct" ? "circle-correct" : "circle-wrong";
    marker.style.position = "absolute";
    marker.style.left = x + "px";
    marker.style.top = y + "px";
    marker.style.pointerEvents = "none";
    marker.style.zIndex = "999";

    if (type === "wrong") {
        marker.textContent = "✕";
        setTimeout(function() {
            if (marker.parentElement) marker.parentElement.removeChild(marker);
        }, 1000);
    }

    var box = document.getElementById(
        img.id === "img-modified" ? "box-modified" : "box-original"
    );
    box.appendChild(marker);
}

function finishGame() {
    document.getElementById("hiddenSeconds").value = secondsUsed || 0;
    document.getElementById("hiddenFound2").value = foundCount || 0;
    document.getElementById("finishForm").submit();
}