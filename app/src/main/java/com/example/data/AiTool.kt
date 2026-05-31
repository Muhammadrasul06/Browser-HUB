package com.example.data

data class AiTool(
    val name: String,
    val category: String,
    val websiteUrl: String,
    val logoUrl: String? = null,
    val goodFor: String,
    val pricing: String = "Unknown" // Free, Paid, Freemium, Unknown
)

object AiToolsProvider {
    val tools: List<AiTool> = listOf(
        // Chatbots
        AiTool(
            name = "ChatGPT",
            category = "Chatbots",
            websiteUrl = "https://chatgpt.com",
            logoUrl = "https://chatgpt.com/favicon.ico",
            goodFor = "General reasoning, text generation, multi-turn conversations, custom GPT models, and daily assistances.",
            pricing = "Freemium"
        ),
        AiTool(
            name = "Claude",
            category = "Chatbots",
            websiteUrl = "https://claude.ai",
            logoUrl = "https://claude.ai/favicon.ico",
            goodFor = "Exceptional academic-level writing structure, long document analysis, logical coding, and research reading.",
            pricing = "Freemium"
        ),
        AiTool(
            name = "Mistral Le Chat",
            category = "Chatbots",
            websiteUrl = "https://chat.mistral.ai",
            logoUrl = "https://chat.mistral.ai/favicon.ico",
            goodFor = "Fast multi-lingual conversing and strong support for European localized regulatory and community models.",
            pricing = "Free"
        ),
        AiTool(
            name = "Character.AI",
            category = "Chatbots",
            websiteUrl = "https://character.ai",
            logoUrl = "https://character.ai/favicon.ico",
            goodFor = "Immersive dialogue roleplay, entertainment characters, personal mentors, and customized companion bots.",
            pricing = "Freemium"
        ),
        AiTool(
            name = "Poe",
            category = "Chatbots",
            websiteUrl = "https://poe.com",
            logoUrl = "https://poe.com/favicon.ico",
            goodFor = "Central hub to test and compare multiple leading artificial models in one streamlined interface.",
            pricing = "Freemium"
        ),

        // Writing
        AiTool(
            name = "Grammarly",
            category = "Writing",
            websiteUrl = "https://www.grammarly.com",
            logoUrl = "https://www.grammarly.com/favicon.ico",
            goodFor = "Real-time grammar corrections, tone adjustments, spelling checks, and high-impact essays refinement.",
            pricing = "Freemium"
        ),
        AiTool(
            name = "Jasper",
            category = "Writing",
            websiteUrl = "https://www.jasper.ai",
            logoUrl = "https://www.jasper.ai/favicon.ico",
            goodFor = "SEO blog planning, corporate copy structures, marketing template tools, and collaborative team editing.",
            pricing = "Paid"
        ),
        AiTool(
            name = "Copy.ai",
            category = "Writing",
            websiteUrl = "https://www.copy.ai",
            logoUrl = "https://www.copy.ai/favicon.ico",
            goodFor = "Automated sales text messaging, go-to-market workflows, and direct lead emails writing optimization.",
            pricing = "Freemium"
        ),
        AiTool(
            name = "Writesonic",
            category = "Writing",
            websiteUrl = "https://writesonic.com",
            logoUrl = "https://writesonic.com/favicon.ico",
            goodFor = "AI article outline planner, landing page text generator, and high-convert social copy ideas creator.",
            pricing = "Freemium"
        ),
        AiTool(
            name = "QuillBot",
            category = "Writing",
            websiteUrl = "https://quillbot.com",
            logoUrl = "https://quillbot.com/favicon.ico",
            goodFor = "Paraphrasing complex sentences to improve reading flow, synonym checking, and citation managing.",
            pricing = "Freemium"
        ),

        // Coding
        AiTool(
            name = "Cursor",
            category = "Coding",
            websiteUrl = "https://cursor.com",
            logoUrl = "https://cursor.com/favicon.ico",
            goodFor = "Advanced AI code complete, repository context chat, multi-file edits, and inline agent guidance.",
            pricing = "Freemium"
        ),
        AiTool(
            name = "Windsurf",
            category = "Coding",
            websiteUrl = "https://windsurf.com",
            logoUrl = "https://windsurf.com/favicon.ico",
            goodFor = "Agentic software building with Cascade, workspace-wide project refactoring, and file edit loops.",
            pricing = "Freemium"
        ),
        AiTool(
            name = "GitHub Copilot",
            category = "Coding",
            websiteUrl = "https://github.com/features/copilot",
            logoUrl = "https://github.com/favicon.ico",
            goodFor = "IDE autocomplete suggestions, real-time code fixes inside editors, and automated PR document generation.",
            pricing = "Paid"
        ),
        AiTool(
            name = "Replit Agent",
            category = "Coding",
            websiteUrl = "https://replit.com/ai",
            logoUrl = "https://replit.com/favicon.ico",
            goodFor = "Zero-setup prompt-to-app code compiler, rapid cloud sandbox hosting, and active preview displays.",
            pricing = "Paid"
        ),
        AiTool(
            name = "Bolt.new",
            category = "Coding",
            websiteUrl = "https://bolt.new",
            logoUrl = "https://bolt.new/favicon.ico",
            goodFor = "Full-stack web application development directly in browsers with visual previews and instant bundle exports.",
            pricing = "Freemium"
        ),
        AiTool(
            name = "v0",
            category = "Coding",
            websiteUrl = "https://v0.dev",
            logoUrl = "https://v0.dev/favicon.ico",
            goodFor = "Generating modern visual React, Next.js, and Tailwind code skeletons with accessible Material schemas.",
            pricing = "Freemium"
        ),
        AiTool(
            name = "Devin",
            category = "Coding",
            websiteUrl = "https://cognition.ai",
            logoUrl = "https://cognition.ai/favicon.ico",
            goodFor = "Autonomous developer agent managing complete lifecycle from environment compilation to testing and fixes.",
            pricing = "Paid"
        ),

        // Image
        AiTool(
            name = "Midjourney",
            category = "Image",
            websiteUrl = "https://www.midjourney.com",
            logoUrl = "https://www.midjourney.com/favicon.ico",
            goodFor = "Premium artistic image outputs, exceptional canvas textures, photorealistic scene styling, and prompt precision.",
            pricing = "Paid"
        ),
        AiTool(
            name = "Adobe Firefly",
            category = "Image",
            websiteUrl = "https://www.adobe.com/products/firefly.html",
            logoUrl = "https://www.adobe.com/favicon.ico",
            goodFor = "Commercially safe design image edits, vector layout recolor, generative fills, and custom graphic assets.",
            pricing = "Freemium"
        ),
        AiTool(
            name = "Stable Diffusion",
            category = "Image",
            websiteUrl = "https://stability.ai",
            logoUrl = "https://stability.ai/favicon.ico",
            goodFor = "Tailored model tuning with custom weights, precise UI controls, and direct local device image compile.",
            pricing = "Freemium"
        ),
        AiTool(
            name = "Leonardo AI",
            category = "Image",
            websiteUrl = "https://leonardo.ai",
            logoUrl = "https://leonardo.ai/favicon.ico",
            goodFor = "High quality game environment and character sheets generation, texture assets, and design canvas.",
            pricing = "Freemium"
        ),
        AiTool(
            name = "Ideogram",
            category = "Image",
            websiteUrl = "https://ideogram.ai",
            logoUrl = "https://ideogram.ai/favicon.ico",
            goodFor = "Amazing typographic layouts, poster assets with readable crisp texts, and creative visual alignment.",
            pricing = "Freemium"
        ),
        AiTool(
            name = "FLUX",
            category = "Image",
            websiteUrl = "https://blackforestlabs.ai",
            logoUrl = "https://blackforestlabs.ai/favicon.ico",
            goodFor = "Incredible human proportions rendering, natural details lighting, and close alignment to written prompt inputs.",
            pricing = "Freemium"
        ),

        // Video
        AiTool(
            name = "Runway",
            category = "Video",
            websiteUrl = "https://runwayml.com",
            logoUrl = "https://runwayml.com/favicon.ico",
            goodFor = "Text-to-video generation, style visual swaps, detailed pan camera controls, and movie-grade visual designs.",
            pricing = "Freemium"
        ),
        AiTool(
            name = "Pika",
            category = "Video",
            websiteUrl = "https://pika.art",
            logoUrl = "https://pika.art/favicon.ico",
            goodFor = "Cartoon and gaming assets dynamic animation, facial actions synchronization, and rapid visual sound integration.",
            pricing = "Freemium"
        ),
        AiTool(
            name = "Luma Dream Machine",
            category = "Video",
            websiteUrl = "https://lumalabs.ai",
            logoUrl = "https://lumalabs.ai/favicon.ico",
            goodFor = "Highly continuous spatial cinematic pans, sweeping 3D camera tracks, and physical properties alignments.",
            pricing = "Freemium"
        ),
        AiTool(
            name = "OpenAI Sora",
            category = "Video",
            websiteUrl = "https://sora.com",
            logoUrl = "https://sora.com/favicon.ico",
            goodFor = "Physically logical environmental responses, consistent characters across multiple angles, up to 60-second scenes.",
            pricing = "Paid"
        ),
        AiTool(
            name = "HeyGen",
            category = "Video",
            websiteUrl = "https://www.heygen.com",
            logoUrl = "https://www.heygen.com/favicon.ico",
            goodFor = "Professional-grade talking human avatars creation for video ads, dynamic multi-language lip sync translations.",
            pricing = "Freemium"
        ),

        // Audio
        AiTool(
            name = "ElevenLabs",
            category = "Audio",
            websiteUrl = "https://elevenlabs.io",
            logoUrl = "https://elevenlabs.io/favicon.ico",
            goodFor = "Top-tier organic voice synthesis, vocal clone creation, multi-language speech narrates, and ambient sounds effects.",
            pricing = "Freemium"
        ),
        AiTool(
            name = "Suno",
            category = "Audio",
            websiteUrl = "https://suno.com",
            logoUrl = "https://suno.com/favicon.ico",
            goodFor = "Generating radio-ready songs, customized musical instrumentation, multi-voice vocals, and lyrics formatting.",
            pricing = "Freemium"
        ),
        AiTool(
            name = "Udio",
            category = "Audio",
            websiteUrl = "https://udio.com",
            logoUrl = "https://udio.com/favicon.ico",
            goodFor = "High creative fidelity song structures mixing, vocal stems split adjusts, and advanced music extensions.",
            pricing = "Freemium"
        ),

        // Productivity
        AiTool(
            name = "Notion AI",
            category = "Productivity",
            websiteUrl = "https://www.notion.so/product/ai",
            logoUrl = "https://www.notion.so/favicon.ico",
            goodFor = "Document summaries, workspace databases search filters, automated task updates, and meeting notes revision.",
            pricing = "Paid"
        ),
        AiTool(
            name = "Otter.ai",
            category = "Productivity",
            websiteUrl = "https://otter.ai",
            logoUrl = "https://otter.ai/favicon.ico",
            goodFor = "Real-time calendar meeting transcription, visual slide capturing, search tags, to-do lists extracts and summaries.",
            pricing = "Freemium"
        ),
        AiTool(
            name = "Fathom",
            category = "Productivity",
            websiteUrl = "https://fathom.video",
            logoUrl = "https://fathom.video/favicon.ico",
            goodFor = "Integrated screen recorder and high-speed dashboard for CRM automatic notes compilation from voice recordings.",
            pricing = "Freemium"
        ),

        // Research
        AiTool(
            name = "Perplexity",
            category = "Research",
            websiteUrl = "https://www.perplexity.ai",
            logoUrl = "https://www.perplexity.ai/favicon.ico",
            goodFor = "Factual real-time web search results citations, academic publication indexing, and deep context digests.",
            pricing = "Freemium"
        ),
        AiTool(
            name = "NotebookLM",
            category = "Research",
            websiteUrl = "https://notebooklm.google",
            logoUrl = "https://notebooklm.google/favicon.ico",
            goodFor = "Grounding AI queries directly on user-provided PDF catalogs, research documents, and audio podcast creation.",
            pricing = "Free"
        ),
        AiTool(
            name = "Consensus",
            category = "Research",
            websiteUrl = "https://consensus.app",
            logoUrl = "https://consensus.app/favicon.ico",
            goodFor = "Mapping peer-reviewed scientific journals facts and gathering evidence-focused replies to medical/technical prompts.",
            pricing = "Freemium"
        ),

        // Design
        AiTool(
            name = "Canva AI",
            category = "Design",
            websiteUrl = "https://www.canva.com/canva-ai/",
            logoUrl = "https://www.canva.com/favicon.ico",
            goodFor = "Dynamic flyer/slide templates styling, instant image background removal, and simple UI assets.",
            pricing = "Freemium"
        ),
        AiTool(
            name = "Figma AI",
            category = "Design",
            websiteUrl = "https://www.figma.com/ai/",
            logoUrl = "https://www.figma.com/favicon.ico",
            goodFor = "UI mockup layer organization assistants, contextual element suggestions, and fast wireframe drafting.",
            pricing = "Freemium"
        ),
        AiTool(
            name = "Uizard",
            category = "Design",
            websiteUrl = "https://uizard.io",
            logoUrl = "https://uizard.io/favicon.ico",
            goodFor = "AI drawing converter transferring pencil sketches to interactive digital product screens and layouts.",
            pricing = "Freemium"
        ),

        // Education
        AiTool(
            name = "Khanmigo",
            category = "Education",
            websiteUrl = "https://www.khanmigo.ai",
            logoUrl = "https://www.khanmigo.ai/favicon.ico",
            goodFor = "Interactive curriculum-aligned coursework coaching dashboard, coding exercise checkers, and study guides.",
            pricing = "Paid"
        ),
        AiTool(
            name = "Duolingo Max",
            category = "Education",
            websiteUrl = "https://www.duolingo.com/super",
            logoUrl = "https://www.duolingo.com/favicon.ico",
            goodFor = "Scenario conversational audio simulators, and instant grammar reasoning explainers for vocabulary lessons.",
            pricing = "Paid"
        ),
        AiTool(
            name = "Quizlet AI",
            category = "Education",
            websiteUrl = "https://quizlet.com",
            logoUrl = "https://quizlet.com/favicon.ico",
            goodFor = "Self-study flashcard lists organizer, custom interactive Q-Chat dialogue trainers, and practice tests setups.",
            pricing = "Freemium"
        ),

        // Business
        AiTool(
            name = "Salesforce Einstein",
            category = "Business",
            websiteUrl = "https://www.salesforce.com/artificial-intelligence/",
            logoUrl = "https://www.salesforce.com/favicon.ico",
            goodFor = "Customer relationship management database action pipelines tracking, client data trends insight reports, automation.",
            pricing = "Paid"
        ),
        AiTool(
            name = "HubSpot AI",
            category = "Business",
            websiteUrl = "https://www.hubspot.com/artificial-intelligence",
            logoUrl = "https://www.hubspot.com/favicon.ico",
            goodFor = "Integrated email templates planner, corporate contacts manager, and campaign outcome predictions models.",
            pricing = "Freemium"
        ),
        AiTool(
            name = "Gong AI",
            category = "Business",
            websiteUrl = "https://www.gong.io",
            logoUrl = "https://www.gong.io/favicon.ico",
            goodFor = "B2B client negotiation call analyses, automated sales pipeline alerts, and deal trajectory risk detection.",
            pricing = "Paid"
        ),

        // Automation
        AiTool(
            name = "Zapier Agents",
            category = "Automation",
            websiteUrl = "https://zapier.com/agents",
            logoUrl = "https://zapier.com/favicon.ico",
            goodFor = "Structuring robust agent actions across multiple linked platform accounts, calendar bookings, and CRM updates.",
            pricing = "Freemium"
        ),
        AiTool(
            name = "n8n",
            category = "Automation",
            websiteUrl = "https://n8n.io",
            logoUrl = "https://n8n.io/favicon.ico",
            goodFor = "Designing developer-focused visual logic steps with direct webhooks, database hooks, and custom script integrations.",
            pricing = "Freemium"
        ),
        AiTool(
            name = "Make",
            category = "Automation",
            websiteUrl = "https://www.make.com",
            logoUrl = "https://www.make.com/favicon.ico",
            goodFor = "Connecting systems visually in scenarios with rich logical data routing, formatting filters, and timing schedules.",
            pricing = "Freemium"
        ),

        // Other
        AiTool(
            name = "HuggingChat",
            category = "Other",
            websiteUrl = "https://huggingface.co/chat",
            logoUrl = "https://huggingface.co/favicon.ico",
            goodFor = "Testing cutting-edge open weights models uploaded by community developers in an open web chat portal.",
            pricing = "Free"
        ),
        AiTool(
            name = "Pi",
            category = "Other",
            websiteUrl = "https://pi.ai",
            logoUrl = "https://pi.ai/favicon.ico",
            goodFor = "Warm casual dialogue conversations, creative brainstorming sessions, and supportive empathetic chats.",
            pricing = "Free"
        )
    )
}
