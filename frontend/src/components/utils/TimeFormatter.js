export function formatTime(timeToFormat) {
    const DAY = 86400000;
    const timeSince = Date.now() - timeToFormat * 1000;
    const dateToFormat = new Date(timeToFormat * 1000);
    if (timeSince < DAY) {
        return "Today";
    } else if (timeSince < DAY * 2) {
        return "Yesterday";
    } else if (timeSince > DAY * 30) {
        return dateToFormat.toLocaleDateString();
    } else {
        return Math.round(timeSince / DAY) + " days ago";
    }
}
