<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    </head>
    <body>
        <p>Hi ${username},</p>
        <p>You have ${numUnread} unread <#if numUnread?number gt 1>messages<#else>message</#if> on projectideas. See your unread messages at <a href="https://projectideas.tech/messages" rel="link">projectideas.tech/messages</a>.</p>
        <p>Regards,<br />
            projectideas
        </p>
        <a href=${unsubscribeLink} rel="link">Unsubscribe</a>
    </body>
</html>