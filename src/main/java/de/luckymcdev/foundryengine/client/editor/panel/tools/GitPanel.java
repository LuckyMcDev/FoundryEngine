package de.luckymcdev.foundryengine.client.editor.panel.tools;

import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.imgui.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.client.imgui.text.ImGuiCoreTextEditor;
import de.luckymcdev.foundryengine.client.imgui.text.color.IEditorColorizer;
import de.luckymcdev.foundryengine.client.imgui.text.editor.EditorTheme;
import de.luckymcdev.foundryengine.client.imgui.text.preset.git.GitColorizer;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.service.EngineServiceResult;
import de.luckymcdev.foundryengine.common.service.GitService;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiSelectableFlags;
import imgui.type.ImInt;
import imgui.type.ImString;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class GitPanel extends EngineServicePanel<GitService> {
	public static final GitPanel INSTANCE = new GitPanel();

	private static final int TAB_STATUS = 0;
	private static final int TAB_DIFF = 1;
	private static final int TAB_LOG = 2;
	private static final int TAB_BRANCH = 3;
	private static final int TAB_REMOTE = 4;
	private static final int TAB_STASH = 5;
	private static final int TAB_TAGS = 6;

	private final ImGuiCoreTextEditor diffViewer;

	private final ImString refInput = new ImString(128);
	private final ImString commitMessage = new ImString(256);
	private final ImString remoteName = new ImString(64);
	private final ImString remoteUrl = new ImString(256);
	private final ImString pushRemote = new ImString(64);
	private final ImString pushBranch = new ImString(128);
	private final ImString stashMessage = new ImString(128);
	private final ImString tagName = new ImString(64);
	private final ImString tagMessage = new ImString(256);
	private final ImString tagCommit = new ImString(64);
	private final ImInt stashIndex = new ImInt(0);
	private final List<BranchEntry> branches = new ArrayList<>();
	private String currentBranch = "";
	private String currentCommit = "";
	private int logLimit = 20;
	private boolean branchesLoaded = false;

	private GitPanel() {
		super(new Builder(Common.id("git"))
			.icon(ImIcons.CODE_BRANCH)
			.category(PanelCategory.TOOLS)
			.menuBar(true));

		EditorTheme diffTheme = EditorTheme.monokai().build();
		diffViewer = ImGuiCoreTextEditor.createForLanguage(ImGuiCoreTextEditor.Language.DIFF, diffTheme);
		diffViewer.setReadOnly(true);
	}

	@Override
	protected IEditorColorizer createOutputColorizer() {
		return new GitColorizer();
	}

	@Override
	protected String[] getTabNames() {
		return new String[]{"Status", "Diff", "Log", "Branch", "Remote", "Stash", "Tags"};
	}

	@Override
	protected GitService getService() {
		return Common.getEngineServiceManager().get(GitService.class).orElse(null);
	}

	@Override
	protected void onTabChanged(int tab, GitService git) {
		switch (tab) {
			case TAB_STATUS -> {
				refreshStatus(git);
				run(git.status(), "Status");
			}
			case TAB_LOG -> run(git.log(logLimit), "Log");
			case TAB_BRANCH -> {
				run(git.branch(), "Branch");
				loadBranches(git);
			}
			case TAB_REMOTE -> run(git.remoteList(), "Remote List");
			case TAB_STASH -> run(git.stashList(), "Stash List");
			case TAB_TAGS -> run(git.tagList(), "Tag List");
			case TAB_DIFF -> {
			}
		}
	}

	@Override
	protected void renderTabContent(ImGraphicsExtractor g, GitService git) {
		switch (activeTab) {
			case TAB_STATUS -> renderStatusTab(g, git);
			case TAB_DIFF -> renderDiffTab(g, git);
			case TAB_LOG -> renderLogTab(g, git);
			case TAB_BRANCH -> renderBranchTab(g, git);
			case TAB_REMOTE -> renderRemoteTab(g, git);
			case TAB_STASH -> renderStashTab(g, git);
			case TAB_TAGS -> renderTagsTab(g, git);
		}
	}

	@Override
	protected void extraMenuItems(GitService git) {
		if (activeTab == TAB_DIFF) {
			if (ImGui.menuItem(ImIcons.COPY + " Copy")) {
				ImGui.setClipboardText(diffViewer.getText());
			}
		}
	}

	@Override
	protected String customNotAvailableMessage() {
		return "To install, visit: https://git-scm.com/";
	}

	private void renderStatusTab(ImGraphicsExtractor g, GitService git) {
		g.cardBegin("##git_info_card");
		ImGui.text(ImIcons.CODE_BRANCH + "  Branch:  ");
		ImGui.sameLine();
		ImGui.textColored(0xFF4CAF50, currentBranch.isEmpty() ? "—" : currentBranch);
		ImGui.text(ImIcons.TAG + "  Commit:  ");
		ImGui.sameLine();
		ImGui.textDisabled(currentCommit.isEmpty() ? "—" : currentCommit);
		g.cardEnd();

		ImGui.spacing();

		g.cardBegin("##git_status_actions");
		ImGui.text(ImIcons.FOLDER_OPEN + "  Working Tree");
		ImGui.spacing();

		if (button(ImIcons.LIST + " Status")) {
			run(git.status(), "Status");
		}
		ImGui.sameLine();
		if (button(ImIcons.PLUS + " Add All")) {
			run(git.addAll(), "Add All");
		}

		ImGui.spacing();
		ImGui.text(ImIcons.PENCIL + "  Commit");
		ImGui.spacing();

		ImGui.setNextItemWidth(-120);
		ImGui.inputTextWithHint("##commit_msg", "Commit message...", commitMessage, ImGuiInputTextFlags.None);
		ImGui.sameLine();
		if (button(ImIcons.CHECK + " Commit")) {
			if (!commitMessage.isEmpty()) {
				String msg = commitMessage.get();
				run(git.addAll().thenCompose(r -> git.commit(msg)), "Commit");
				commitMessage.set("");
			}
		}

		ImGui.spacing();
		if (button(ImIcons.UNDO + " Amend (no edit)")) {
			run(git.commitAmendNoEdit(), "Amend");
		}
		g.helpTooltip("Amend the last commit without changing its message.");
		g.cardEnd();

		ImGui.spacing();

		g.cardBegin("##git_cleanup_card");
		ImGui.text(ImIcons.BROOM + "  Cleanup");
		ImGui.spacing();
		if (button(ImIcons.TRASH + " Clean (files)")) {
			run(git.clean(true, false), "Clean");
		}
		ImGui.sameLine();
		if (button(ImIcons.TRASH + " Clean (+ dirs)")) {
			run(git.clean(true, true), "Clean (dirs)");
		}
		g.cardEnd();

		if (currentBranch.isEmpty()) {
			refreshStatus(git);
		}
	}

	private void renderDiffTab(ImGraphicsExtractor g, GitService git) {
		g.cardBegin("##git_diff_controls");
		ImGui.text(ImIcons.SEARCH + "  Diff");
		ImGui.spacing();

		if (button(ImIcons.SEARCH + " Working Tree")) {
			runDiff(git.diff(), "Diff (Working Tree)");
		}
		ImGui.sameLine();
		if (button(ImIcons.SEARCH + " Staged")) {
			runDiff(git.diffStaged(), "Diff (Staged)");
		}

		ImGui.spacing();

		ImGui.setNextItemWidth(-120);
		ImGui.inputTextWithHint("##diff_ref", "Commit / branch / ref...", refInput, ImGuiInputTextFlags.None);
		ImGui.sameLine();
		if (button(ImIcons.SEARCH + " Show")) {
			if (!refInput.isEmpty()) {
				runDiff(git.show(refInput.get()), "Show " + refInput.get());
			}
		}
		g.helpTooltip("Show a specific commit or ref as a diff.");
		g.cardEnd();

		ImGui.spacing();

		if (running) {
			ImGui.textDisabled(ImIcons.SPINNER + "  Fetching diff...");
			return;
		}

		float availH = ImGui.getContentRegionAvailY();
		diffViewer.render("##diff_view", ImGui.getContentRegionAvailX(), availH, false);
	}

	private void renderLogTab(ImGraphicsExtractor g, GitService git) {
		g.cardBegin("##git_log_card");
		ImGui.text(ImIcons.HISTORY + "  Commit History");
		ImGui.spacing();

		ImGui.text("Limit: ");
		ImGui.sameLine();
		ImGui.setNextItemWidth(80);
		int[] limitRef = {logLimit};
		if (ImGui.sliderInt("##log_limit", limitRef, 5, 100)) {
			logLimit = limitRef[0];
		}

		ImGui.spacing();

		if (button(ImIcons.LIST + " Log")) {
			run(git.log(logLimit), "Log");
		}
		ImGui.sameLine();
		if (button(ImIcons.LIST + " Oneline")) {
			run(git.logOneline(logLimit), "Log Oneline");
		}
		ImGui.sameLine();
		if (button(ImIcons.SITEMAP + " Graph")) {
			run(git.logGraph(logLimit), "Log Graph");
		}
		ImGui.sameLine();
		if (button(ImIcons.TAG + " Describe Tag")) {
			run(git.describeTag(), "Describe Tag");
		}

		ImGui.spacing();
		ImGui.separator();
		ImGui.spacing();

		ImGui.setNextItemWidth(-120);
		ImGui.inputTextWithHint("##show_ref", "Commit hash or ref...", refInput, ImGuiInputTextFlags.None);
		ImGui.sameLine();
		if (button(ImIcons.EYE + " Show in Diff")) {
			if (!refInput.isEmpty()) {
				activeTab = TAB_DIFF;
				runDiff(git.show(refInput.get()), "Show " + refInput.get());
			}
		}
		g.helpTooltip("Opens the commit in the Diff viewer.");
		g.cardEnd();
	}

	private void renderBranchTab(ImGraphicsExtractor g, GitService git) {
		g.cardBegin("##git_branch_list_card");
		ImGui.text(ImIcons.CODE_BRANCH + "  Branches");
		ImGui.spacing();

		if (branchesLoaded && !branches.isEmpty()) {
			ImGui.text("Click a branch to check it out:");
			ImGui.spacing();

			float listHeight = Math.min(200, branches.size() * ImGui.getFontSize() * 1.2f);
			ImGui.beginChild("##branch_list", 0, listHeight, true);
			for (BranchEntry entry : branches) {
				boolean isCurrent = entry.isCurrent;
				if (isCurrent) {
					ImGui.pushStyleColor(ImGuiCol.Text, 0xFF4CAF50);
					ImGui.pushStyleColor(ImGuiCol.Header, 0x334CAF50);
				}
				if (ImGui.selectable((isCurrent ? "* " : "  ") + entry.name, false, ImGuiSelectableFlags.None)) {
					if (!entry.isCurrent) {
						run(git.checkout(entry.name), "Checkout " + entry.name);
						loadBranches(git);
					}
				}
				if (isCurrent) {
					ImGui.popStyleColor(2);
				}
			}
			ImGui.endChild();
		} else {
			ImGui.textDisabled("No branches loaded. Click Refresh or switch to this tab.");
		}

		ImGui.spacing();
		if (button(ImIcons.REFRESH + " Refresh List")) {
			loadBranches(git);
		}
		g.cardEnd();

		ImGui.spacing();

		g.cardBegin("##git_branch_ops_card");
		ImGui.text(ImIcons.PLUS + "  Create / Checkout");
		ImGui.spacing();

		ImGui.setNextItemWidth(-1);
		ImGui.inputTextWithHint("##branch_ref", "Branch name...", refInput, ImGuiInputTextFlags.None);
		ImGui.spacing();

		if (button(ImIcons.PLUS + " Create")) {
			if (!refInput.isEmpty()) {
				run(git.branchCreate(refInput.get()), "Create Branch");
				loadBranches(git);
			}
		}
		ImGui.sameLine();
		if (button(ImIcons.SIGN_OUT + " Checkout")) {
			if (!refInput.isEmpty()) {
				run(git.checkout(refInput.get()), "Checkout");
				loadBranches(git);
			}
		}
		ImGui.sameLine();
		if (button(ImIcons.SIGN_OUT + " Create & Checkout")) {
			if (!refInput.isEmpty()) {
				run(git.checkoutNew(refInput.get()), "Create & Checkout");
				loadBranches(git);
			}
		}

		ImGui.spacing();

		if (button(ImIcons.TRASH + " Delete (safe)")) {
			if (!refInput.isEmpty()) {
				run(git.branchDelete(refInput.get()), "Delete Branch");
				loadBranches(git);
			}
		}
		ImGui.sameLine();
		if (button(ImIcons.EXCLAMATION_TRIANGLE + " Force Delete")) {
			if (!refInput.isEmpty()) {
				run(git.branchDeleteForce(refInput.get()), "Force Delete");
				loadBranches(git);
			}
		}
		g.cardEnd();

		ImGui.spacing();

		g.cardBegin("##git_merge_card");
		ImGui.text(ImIcons.CODE_BRANCH + "  Merge / Rebase");
		ImGui.spacing();

		if (button(ImIcons.CODE_BRANCH + " Merge")) {
			if (!refInput.isEmpty()) {
				run(git.merge(refInput.get()), "Merge");
			}
		}
		ImGui.sameLine();
		if (button(ImIcons.TIMES + " Abort Merge")) {
			run(git.mergeAbort(), "Abort Merge");
		}

		ImGui.spacing();

		if (button(ImIcons.EXCHANGE + " Rebase")) {
			if (!refInput.isEmpty()) {
				run(git.rebase(refInput.get()), "Rebase");
			}
		}
		ImGui.sameLine();
		if (button(ImIcons.TIMES + " Abort Rebase")) {
			run(git.rebaseAbort(), "Abort Rebase");
		}
		ImGui.sameLine();
		if (button(ImIcons.CHECK + " Continue Rebase")) {
			run(git.rebaseContinue(), "Continue Rebase");
		}
		g.cardEnd();
	}

	private void renderRemoteTab(ImGraphicsExtractor g, GitService git) {
		g.cardBegin("##git_sync_card");
		ImGui.text(ImIcons.CLOUD + "  Sync");
		ImGui.spacing();

		if (button(ImIcons.DOWNLOAD + " Pull")) {
			run(git.pull(), "Pull");
		}
		ImGui.sameLine();
		if (button(ImIcons.EXCHANGE + " Pull --rebase")) {
			run(git.pullRebase(), "Pull (rebase)");
		}
		ImGui.sameLine();
		if (button(ImIcons.UPLOAD + " Push")) {
			run(git.push(), "Push");
		}
		ImGui.sameLine();
		if (button(ImIcons.EXCLAMATION_TRIANGLE + " Force Push")) {
			run(git.pushForce(), "Force Push");
		}
		g.helpTooltip("Uses --force-with-lease.");

		ImGui.spacing();

		if (button(ImIcons.REFRESH + " Fetch")) {
			run(git.fetch(), "Fetch");
		}
		ImGui.sameLine();
		if (button(ImIcons.REFRESH + " Fetch All")) {
			run(git.fetchAll(), "Fetch All");
		}
		ImGui.sameLine();
		if (button(ImIcons.REFRESH + " Fetch + Prune")) {
			run(git.fetchPrune(), "Fetch + Prune");
		}
		ImGui.sameLine();
		if (button(ImIcons.UPLOAD + " Push Tags")) {
			run(git.pushTags(), "Push Tags");
		}
		g.cardEnd();

		ImGui.spacing();

		g.cardBegin("##git_upstream_card");
		ImGui.text(ImIcons.SIGN_OUT + "  Set Upstream");
		ImGui.spacing();

		ImGui.setNextItemWidth(120);
		ImGui.inputTextWithHint("##push_remote", "Remote (origin)", pushRemote, ImGuiInputTextFlags.None);
		ImGui.sameLine();
		ImGui.setNextItemWidth(-120);
		ImGui.inputTextWithHint("##push_branch", "Branch (main)", pushBranch, ImGuiInputTextFlags.None);
		ImGui.sameLine();
		if (button(ImIcons.UPLOAD + " Push")) {
			String r = pushRemote.isEmpty() ? "origin" : pushRemote.get();
			String b = pushBranch.isEmpty() ? "main" : pushBranch.get();
			run(git.pushSetUpstream(r, b), "Push Upstream");
		}
		g.cardEnd();

		ImGui.spacing();

		g.cardBegin("##git_remotes_card");
		ImGui.text(ImIcons.SERVER + "  Remotes");
		ImGui.spacing();

		if (button(ImIcons.LIST + " List Remotes")) {
			run(git.remoteList(), "Remote List");
		}

		ImGui.spacing();

		ImGui.setNextItemWidth(120);
		ImGui.inputTextWithHint("##remote_name", "Name (origin)", remoteName, ImGuiInputTextFlags.None);
		ImGui.sameLine();
		ImGui.setNextItemWidth(-120);
		ImGui.inputTextWithHint("##remote_url", "URL...", remoteUrl, ImGuiInputTextFlags.None);
		ImGui.sameLine();

		if (button(ImIcons.PLUS + " Add")) {
			if (!remoteName.isEmpty() && !remoteUrl.isEmpty()) {
				run(git.remoteAdd(remoteName.get(), remoteUrl.get()), "Add Remote");
			}
		}

		ImGui.spacing();

		if (button(ImIcons.PENCIL + " Set URL")) {
			if (!remoteName.isEmpty() && !remoteUrl.isEmpty()) {
				run(git.remoteSetUrl(remoteName.get(), remoteUrl.get()), "Set Remote URL");
			}
		}
		ImGui.sameLine();
		if (button(ImIcons.TRASH + " Remove")) {
			if (!remoteName.isEmpty()) {
				run(git.remoteRemove(remoteName.get()), "Remove Remote");
			}
		}
		g.cardEnd();
	}

	private void renderStashTab(ImGraphicsExtractor g, GitService git) {
		g.cardBegin("##git_stash_card");
		ImGui.text(ImIcons.ARCHIVE + "  Stash");
		ImGui.spacing();

		if (button(ImIcons.LIST + " List")) {
			run(git.stashList(), "Stash List");
		}

		ImGui.spacing();

		ImGui.setNextItemWidth(-120);
		ImGui.inputTextWithHint("##stash_msg", "Stash message (optional)...", stashMessage, ImGuiInputTextFlags.None);
		ImGui.sameLine();
		if (button(ImIcons.INBOX + " Stash")) {
			if (!stashMessage.isEmpty()) {
				String msg = stashMessage.get();
				run(git.stashPush(msg), "Stash");
				stashMessage.set("");
			}
		}

		ImGui.spacing();

		ImGui.text("Index: ");
		ImGui.sameLine();
		ImGui.setNextItemWidth(80);
		if (ImGui.inputInt("##stash_index", stashIndex)) {
			stashIndex.set(Math.max(0, stashIndex.get()));
		}

		ImGui.spacing();

		if (button(ImIcons.SIGN_OUT + " Pop")) {
			run(git.stashPop(), "Stash Pop");
		}
		ImGui.sameLine();
		if (button(ImIcons.SIGN_OUT + " Apply [n]")) {
			run(git.stashApply(stashIndex.get()), "Stash Apply");
		}
		ImGui.sameLine();
		if (button(ImIcons.TRASH + " Drop [n]")) {
			run(git.stashDrop(stashIndex.get()), "Stash Drop");
		}
		g.cardEnd();
	}

	private void renderTagsTab(ImGraphicsExtractor g, GitService git) {
		g.cardBegin("##git_tags_card");
		ImGui.text(ImIcons.TAG + "  Tags");
		ImGui.spacing();

		if (button(ImIcons.LIST + " List Tags")) {
			run(git.tagList(), "Tag List");
		}
		ImGui.sameLine();
		if (button(ImIcons.TAG + " Describe")) {
			run(git.describeTag(), "Describe Tag");
		}

		ImGui.spacing();
		ImGui.separator();
		ImGui.spacing();

		ImGui.setNextItemWidth(-1);
		ImGui.inputTextWithHint("##tag_name", "Tag name (e.g. v1.2.0)...", tagName, ImGuiInputTextFlags.None);
		ImGui.spacing();
		ImGui.setNextItemWidth(-1);
		ImGui.inputTextWithHint("##tag_msg", "Annotated tag message (optional)...", tagMessage, ImGuiInputTextFlags.None);
		ImGui.spacing();
		ImGui.setNextItemWidth(-1);
		ImGui.inputTextWithHint("##tag_commit", "Commit (leave empty for HEAD)...", tagCommit, ImGuiInputTextFlags.None);
		ImGui.spacing();

		if (button(ImIcons.PLUS + " Lightweight")) {
			if (!tagName.isEmpty()) {
				if (tagCommit.isEmpty()) {
					run(git.tagCreate(tagName.get()), "Create Tag");
				} else {
					run(git.tagCreateAt(tagName.get(), tagCommit.get()), "Create Tag");
				}
			}
		}
		ImGui.sameLine();
		if (button(ImIcons.PLUS + " Annotated")) {
			if (!tagName.isEmpty() && !tagMessage.isEmpty()) {
				run(git.tagCreateAnnotated(tagName.get(), tagMessage.get()), "Create Annotated Tag");
			}
		}
		g.helpTooltip("Annotated tags store author, date, and message — recommended for mod releases.");
		ImGui.sameLine();
		if (button(ImIcons.TRASH + " Delete")) {
			if (!tagName.isEmpty()) {
				run(git.tagDelete(tagName.get()), "Delete Tag");
			}
		}

		ImGui.spacing();
		if (button(ImIcons.UPLOAD + " Push Tags to Remote")) {
			run(git.pushTags(), "Push Tags");
		}
		g.cardEnd();
	}

	// ---- Helper methods ----

	private void runDiff(CompletableFuture<EngineServiceResult> future, String label) {
		running = true;
		activeTab = TAB_DIFF;
		diffViewer.setText("Loading diff...");
		final String timestamp = timestamp();
		future.thenAccept(result -> {
			running = false;
			String content = result.stdout().isEmpty() ? result.stderr() : result.stdout();
			if (content.isEmpty()) {
				content = result.success() ? "(empty diff)" : "Failed (exit " + result.exitCode() + ")";
			}
			diffViewer.setText(timestamp + label + ":\n" + content);
		});
	}

	private void loadBranches(GitService git) {
		git.branch().thenAccept(result -> {
			if (result.success()) {
				branches.clear();
				String[] lines = result.stdout().split("\n");
				for (String line : lines) {
					if (line.trim().isEmpty()) {
						continue;
					}
					boolean isCurrent = line.startsWith("*");
					String name = line.replace("*", "").trim();
					branches.add(new BranchEntry(name, isCurrent));
				}
				branchesLoaded = true;
			} else {
				branchesLoaded = false;
			}
		});
	}

	private void refreshStatus(GitService git) {
		git.currentBranch().thenAccept(r -> {
			if (r.success()) {
				currentBranch = r.stdout();
			}
		});
		git.currentCommit().thenAccept(r -> {
			if (r.success()) {
				currentCommit = r.stdout().length() > 8 ? r.stdout().substring(0, 8) : r.stdout();
			}
		});
	}

	private record BranchEntry(String name, boolean isCurrent) {
	}
}