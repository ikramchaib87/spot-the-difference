var foundCount = INITIAL_FOUND;
var score = INITIAL_SCORE;
var secondsUsed = 0;
var timerInterval = null;
var gameFinished = false; // ✅ FLAG — bloque tout quand true
var hintsLeft = 3;
var hintCooldown = false;

window.onload = function() {
    document.getElementById("foundCount").textContent = foundCount;
    document.getElementById("scoreDisplay").textContent = score;
    if (TIME_LIMIT > 0) startTimer();
    setInterval(autoSave, 30000);
};

function autoSave() {
    if (gameFinished) return;
    var levelVal = document.getElementById("hiddenLevel").value;
    var ctx = document.querySelector('meta[name="ctx"]').getAttribute('content');

    fetch(ctx + "/save", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: "level=" + levelVal +
              "&differencesFound=" + (foundCount || 0) +
              "&currentScore=" + (score || 0) +
              "&timeRemaining=" + (document.getElementById("hiddenTime").value || 0)
    }).then(function(r) {
        console.log("Save: " + r.status);
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
		{ x: 94.8, y: 52.2, r: 6 }, { x: 84.6, y: 57.3, r: 6 },
		{ x: 76.0, y: 44.5, r: 6 }, { x: 23.0, y: 21.3, r: 6 },
		{ x: 15.2, y: 23.0, r: 6 }, { x: 9.0, y: 69.3, r: 6 },
		{ x: 40.8, y: 81.9, r: 6 }, { x: 67.8, y: 34.8, r: 6 },
		{ x: 35.4, y: 64.5, r: 6 }, { x: 91.2, y: 72.8, r: 6 }
    ],
    3: [
        { x: 62.2, y: 22.8, r: 6 }, { x: 44.4, y: 42.4, r: 6 },
        { x: 85.0, y: 75.9, r: 6 }, { x: 72.6, y: 41.1, r: 6 },
        { x: 77.1, y: 14.3, r: 6 }, { x: 53.3, y: 20.1, r: 6 },
        { x: 22.8, y: 79.9, r: 6 }, { x: 16.5, y: 90.6, r: 6 },
        { x: 9.8,  y: 22.8, r: 6 }, { x: 38.0, y: 96.0, r: 6 }
    ],
    4: [
		{ x: 63.2, y: 27.6, r: 6 }, { x: 24.6, y: 15.6, r: 6 },
		{ x: 85.2, y: 53.3, r: 6 }, { x: 97.0, y: 26.8, r: 6 },
		{ x: 38.0, y: 64.2, r: 6 }, { x: 4.4, y: 46.2, r: 6 },
		{ x: 8.8, y: 23.6, r: 6 },  { x: 42.0, y: 20.2, r: 6 },
		{ x: 41.0, y: 84.2, r: 6 }, { x: 66.4, y: 80.5, r: 6 }
	    ],
    5: [
		{ x: 96.0, y: 83.9, r: 6 }, { x: 84.0, y: 69.3, r: 6 },
		{ x: 71.6, y: 78.5, r: 6 }, { x: 49.4, y: 85.9, r: 6 },
		{ x: 6.8, y: 81.3, r: 6 },  { x: 7.2, y: 32.2, r: 6 },
		{ x: 27.6, y: 15.6, r: 6 }, { x: 86.2, y: 32.8, r: 6 },
	    { x: 42.4, y: 74.5, r: 6 }, { x: 28.8, y: 60.5, r: 6 }
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
			    finishGame();
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

// ── AIDE (LAMPE) ──────────────────────────────────────────

function useHint() {
    if (gameFinished) return;

    // ✅ Vérifie le cooldown (15 min)
    var lastHintTime = localStorage.getItem("lastHintRefill");
    var now = Date.now();

    if (hintsLeft <= 0) {
        if (lastHintTime && (now - parseInt(lastHintTime)) < 15 * 60 * 1000) {
            var remaining = Math.ceil((15 * 60 * 1000 - (now - parseInt(lastHintTime))) / 60000);
            showHintMessage("⏳ Reviens dans " + remaining + " min !");
            return;
        } else {
            // ✅ 15 min passées → recharge 3 aides
            hintsLeft = 3;
            localStorage.removeItem("lastHintRefill");
            updateHintBtn();
        }
    }

    // ✅ Cherche une différence pas encore trouvée
    var levelKey = parseInt(document.getElementById("hiddenLevel").value);
    var levelDiffs = differences[levelKey];
    var notFound = [];

    for (var i = 0; i < levelDiffs.length; i++) {
        if (!foundIndexes.includes(i)) notFound.push(i);
    }

    if (notFound.length === 0) return;

    // ✅ Révèle une différence aléatoire
    var pick = notFound[Math.floor(Math.random() * notFound.length)];
    var diff = levelDiffs[pick];

    var imgModified = document.getElementById("img-modified");
    var imgOriginal = document.getElementById("img-original");

    var x = (diff.x / 100) * imgModified.offsetWidth;
    var y = (diff.y / 100) * imgModified.offsetHeight;
    var origX = (diff.x / 100) * imgOriginal.offsetWidth;
    var origY = (diff.y / 100) * imgOriginal.offsetHeight;

    // ✅ Affiche cercle orange "hint" sur les deux images
    showHintCircle(x, y, imgModified);
    showHintCircle(origX, origY, imgOriginal);

    // ✅ Après 2 secondes, compte comme trouvée automatiquement
    setTimeout(function() {
        if (!foundIndexes.includes(pick)) {
            foundIndexes.push(pick);
            foundCount++;
            score = Math.max(0, score - 50); // moins de points pour une aide

            showCircle(x, y, imgModified, "correct");
            showCircle(origX, origY, imgOriginal, "correct");

            document.getElementById("foundCount").textContent = foundCount;
            document.getElementById("scoreDisplay").textContent = score;
            document.getElementById("hiddenFound").value = foundCount;
            document.getElementById("hiddenScore").value = score;
            document.getElementById("hiddenFound2").value = foundCount;

            if (foundCount >= TOTAL_DIFFERENCES) {
                clearInterval(timerInterval);
                gameFinished = true;
                finishGame();
            }
        }
    }, 3000);

    // ✅ Consomme une aide
    hintsLeft--;
    if (hintsLeft === 0) {
        localStorage.setItem("lastHintRefill", Date.now().toString());
    }
    updateHintBtn();
}

function showHintCircle(x, y, img) {
    var marker = document.createElement("div");
    marker.className = "circle-hint";
    marker.style.position = "absolute";
    marker.style.left = x + "px";
    marker.style.top = y + "px";
    marker.style.pointerEvents = "none";
    marker.style.zIndex = "998";

    var box = document.getElementById(
        img.id === "img-modified" ? "box-modified" : "box-original"
    );
    box.appendChild(marker);

    // Disparaît après 2 secondes
    setTimeout(function() {
        if (marker.parentElement) marker.parentElement.removeChild(marker);
    }, 2000);
}

function updateHintBtn() {
    var btn = document.getElementById("hintBtn");
    var count = document.getElementById("hintCount");
    if (count) count.textContent = hintsLeft;
    if (hintsLeft <= 0) {
        btn.style.opacity = "0.5";
    } else {
        btn.style.opacity = "1";
    }
}

function showHintMessage(msg) {
    var existing = document.getElementById("hintMsg");
    if (existing) existing.remove();
    var div = document.createElement("div");
    div.id = "hintMsg";
    div.textContent = msg;
    div.style.cssText = "position:fixed;top:20px;left:50%;transform:translateX(-50%);background:#ff8c00;color:#fff;padding:12px 24px;border-radius:20px;font-weight:800;z-index:9999;font-family:'Nunito',sans-serif;";
    document.body.appendChild(div);
    setTimeout(function() { if (div.parentElement) div.remove(); }, 3000);
}
/*CALIBRATION - supprimer après
document.addEventListener("DOMContentLoaded", function() {
    var img = document.getElementById("img-modified");
    if (img) {
        img.addEventListener("click", function(e) {
            var rect = this.getBoundingClientRect();
            var x = ((e.clientX - rect.left) / rect.width * 100).toFixed(1);
            var y = ((e.clientY - rect.top) / rect.height * 100).toFixed(1);
            console.log("{ x: " + x + ", y: " + y + ", r: 6 },");
        });
    }
});*/