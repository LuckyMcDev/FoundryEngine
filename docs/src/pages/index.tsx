import type {ReactNode} from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import Heading from '@theme/Heading';

import styles from './index.module.css';

type Section = {
	title: string;
	to: string;
	body: string;
};

const sections: Section[] = [
	{
		title: 'Getting started',
		to: '/docs/getting-started/',
		body: 'Step-by-step lessons that teach you FoundryEngine by building things. Start with your first bundle and work up from there.',
	},
	{
		title: 'Guides',
		to: '/docs/guides/',
		body: 'Recipes and lookups. Add an item, register a command, gate content behind stages, run a server, and look up builders, events, managers, and the glossary.',
	},
	{
		title: 'Concepts',
		to: '/docs/concepts/',
		body: 'Discussions of how the engine works. What a bundle is, how the scripts-to-registry pipeline fits together, why things sync the way they do.',
	},
];

function HomepageHeader() {
	const {siteConfig} = useDocusaurusContext();
	return (
			<header className={clsx('hero hero--primary', styles.heroBanner)}>
				<div className="container">
					<Heading as="h1" className="hero__title">
						{siteConfig.title}
					</Heading>
					<p className="hero__subtitle">{siteConfig.tagline}</p>
					<div className={styles.buttons}>
						<Link
								className="button button--secondary button--lg"
								to="/docs/getting-started/your-first-bundle">
							Get started
						</Link>
					</div>
				</div>
			</header>
	);
}

function QuadrantCard({title, to, body}: Section) {
	return (
			<div className="col col--4">
				<div className={styles.card}>
					<Heading as="h3">
						<Link to={to}>{title}</Link>
					</Heading>
					<p>{body}</p>
				</div>
			</div>
	);
}

export default function Home(): ReactNode {
	return (
			<Layout
					title="FoundryEngine"
					description="Turn Minecraft into a game engine, one bundle at a time.">
				<HomepageHeader/>
				<main>
					<section className={styles.section}>
						<div className="container">
							<div className="row">
								{sections.map((q) => (
										<QuadrantCard key={q.title} {...q} />
								))}
							</div>
						</div>
					</section>
				</main>
			</Layout>
	);
}
