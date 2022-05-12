<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    </head>
    <body>
        <p>Hi ${username},</p>
        <p>You have ${numUnread} unread <#if numUnread?number gt 1>messages<#else>message</#if> on projectideas. See your unread messages at <a href="https://zach332.github.io/projectideas/messages" rel="link">zach332.github.io/projectideas/messages</a>.</p>
        <p>Regards,<br />
            projectideas
        </p>
        <a href=${unsubscribeLink} rel="link">Unsubscribe</a>
    </body>
</html>