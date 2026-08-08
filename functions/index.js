require('dotenv').config();
const express = require('express');
const cors = require('cors');

const app = express();
app.use(cors({ origin: true }));
app.use(express.json());

app.post('/send-to-discord', async (req, res) => {
    try {
        const webhookUrl = process.env.DISCORD_WEBHOOK_URL;
        if (!webhookUrl) {
            return res.status(500).json({ error: 'Webhook URL is not configured.' });
        }

        // Instead of fetching from Firestore, we expect the client to send the note data
        const { title, summary, content, displayName } = req.body;

        if (!title || !content) {
            return res.status(400).json({ error: 'title and content are required fields.' });
        }

        const safeTitle = title || 'Untitled Note';
        const safeSummary = summary || 'No summary generated.';
        const safeDisplayName = displayName || 'A Neuracet Member';

        // Truncate content if it's extremely long to avoid Discord limits
        const maxContentLength = 1000;
        let safeContent = content || '';
        if (safeContent.length > maxContentLength) {
            safeContent = safeContent.substring(0, maxContentLength) + '... (truncated)';
        }

        const discordPayload = {
            username: 'NeuraNotes Bot',
            embeds: [
                {
                    title: safeTitle,
                    description: `**AI Summary:**\n${safeSummary}\n\n**Full Note snippet:**\n${safeContent}`,
                    color: 0x5865F2, // Discord Blurple
                    author: {
                        name: safeDisplayName
                    },
                    timestamp: new Date().toISOString()
                }
            ]
        };

        const response = await fetch(webhookUrl, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(discordPayload)
        });

        if (!response.ok) {
            console.error(`Discord API responded with ${response.status}`);
            return res.status(500).json({ error: 'Failed to send to Discord via API.' });
        }

        return res.json({ success: true, message: 'Message sent to Discord' });

    } catch (error) {
        console.error('Error sending to Discord:', error);
        return res.status(500).json({ error: 'Internal server error.' });
    }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server is running on port ${PORT}`);
});

module.exports = app;
