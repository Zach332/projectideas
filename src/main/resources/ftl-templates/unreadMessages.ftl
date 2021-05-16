<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    </head>
    <body>
        <p>Hi ${username},</p>
        <p>You have ${numUnread} unread messages on projectideas. See your unread messages at https://projectideas.herokuapp.com/messages.</p>
        <p>Regards,<br />
            projectideas
        </p>
        <a href=${unsubscribeLink} rel="link">Unsubscribe</a>
    </body>
</html>