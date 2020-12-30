export function formatTime(timeToFormat) {
    const DAY = 86400000;
    const timeSince = Date.now() - timeToFormat * 1000;
    console.log(timeSince);
    if (timeSince < DAY) {
        return "Today";
    } else if (timeSince < DAY * 2) {
        return "Yesterday";
    } else {
        return Math.round(timeSince / DAY) + " days ago";
    }
}
